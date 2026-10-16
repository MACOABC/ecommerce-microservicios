package com.mercado.productos;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class CatalogoService {
    public record Categoria(Long id, String nombre) {}
    public record CategoriaInput(@NotBlank @Size(max=80) String nombre) {}
    public record Producto(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock, Long categoriaId, Boolean activo) {}
    public record ProductoInput(@NotBlank @Size(max=120) String nombre,
        @NotNull @Size(max=1000) String descripcion,
        @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal precio,
        @NotNull @Min(0) Integer stock, @NotNull @Positive Long categoriaId) {}
    public record Item(@NotNull @Positive Long productoId, @NotNull @Min(1) @Max(999) Integer cantidad) {}
    public record ReservaInput(@NotEmpty @Size(max=100) List<@NotNull @Valid Item> items) {}
    public record Reservado(Long productoId, String nombre, Integer cantidad, BigDecimal precio) {}
    private final JdbcClient db;
    public CatalogoService(JdbcClient db) { this.db = db; }

    @Transactional(readOnly=true)
    public List<Categoria> categorias() { return db.sql("SELECT id,nombre FROM categorias ORDER BY nombre").query(Categoria.class).list(); }
    public Categoria categoria(long id) {
        return db.sql("SELECT id,nombre FROM categorias WHERE id=:id").param("id",id).query(Categoria.class)
            .optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Categoría no encontrada"));
    }
    public Categoria guardarCategoria(Long id, CategoriaInput input) {
        if(id == null) return db.sql("INSERT INTO categorias(nombre) VALUES (:nombre) RETURNING id,nombre")
            .param("nombre",input.nombre().trim()).query(Categoria.class).single();
        categoria(id);
        return db.sql("UPDATE categorias SET nombre=:nombre WHERE id=:id RETURNING id,nombre")
            .param("nombre",input.nombre().trim()).param("id",id).query(Categoria.class).single();
    }
    public void borrarCategoria(long id) {
        categoria(id);
        db.sql("DELETE FROM categorias WHERE id=:id").param("id",id).update();
    }
    @Transactional(readOnly=true)
    public List<Producto> productos(String q, Long categoria) {
        return db.sql("""
            SELECT id,nombre,descripcion,precio,stock,categoria_id,activo FROM productos
            WHERE activo AND (lower(nombre) LIKE :q OR lower(descripcion) LIKE :q)
            AND (:categoria = 0 OR categoria_id=:categoria) ORDER BY id
            """).param("q","%" + q.toLowerCase(Locale.ROOT) + "%").param("categoria",categoria == null ? 0L : categoria)
            .query(Producto.class).list();
    }
    @Transactional(readOnly=true)
    public Producto producto(long id) {
        return db.sql("SELECT * FROM productos WHERE id=:id AND activo").param("id",id).query(Producto.class)
            .optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Producto no encontrado"));
    }
    public Producto guardar(Long id, ProductoInput p) {
        categoria(p.categoriaId());
        if(id == null) return db.sql("""
            INSERT INTO productos(nombre,descripcion,precio,stock,categoria_id) VALUES (:nombre,:descripcion,:precio,:stock,:categoria)
            RETURNING *
            """).param("nombre",p.nombre().trim()).param("descripcion",p.descripcion()).param("precio",p.precio())
            .param("stock",p.stock()).param("categoria",p.categoriaId()).query(Producto.class).single();
        producto(id);
        return db.sql("""
            UPDATE productos SET nombre=:nombre,descripcion=:descripcion,precio=:precio,stock=:stock,categoria_id=:categoria
            WHERE id=:id AND activo RETURNING *
            """).param("id",id).param("nombre",p.nombre().trim()).param("descripcion",p.descripcion()).param("precio",p.precio())
            .param("stock",p.stock()).param("categoria",p.categoriaId()).query(Producto.class).single();
    }
    public void borrar(long id) {
        if(db.sql("UPDATE productos SET activo=FALSE WHERE id=:id AND activo").param("id",id).update()==0)
            throw new ResponseStatusException(NOT_FOUND,"Producto no encontrado");
    }
    public List<Reservado> reservar(UUID orden, ReservaInput input) {
        var items = input.items().stream().sorted(Comparator.comparing(Item::productoId)).toList();
        if(items.stream().map(Item::productoId).distinct().count()!=items.size())
            throw new ResponseStatusException(BAD_REQUEST,"No repitas productos en la reserva");
        int created = db.sql("INSERT INTO reservas(orden_id,estado) VALUES (:id,'RESERVADA') ON CONFLICT DO NOTHING").param("id",orden).update();
        // La inserción concurrente espera al commit; después se bloquea siempre la misma reserva.
        db.sql("SELECT estado FROM reservas WHERE orden_id=:id FOR UPDATE").param("id",orden).query(String.class).single();
        if(created == 0) {
            var previous = reservados(orden);
            if(!previous.stream().map(p -> new Item(p.productoId(),p.cantidad())).toList().equals(items))
                throw new ResponseStatusException(CONFLICT,"La reserva ya existe con otros productos");
            return previous;
        }
        for(var item:items) {
            var p = db.sql("SELECT * FROM productos WHERE id=:id FOR UPDATE").param("id",item.productoId())
                .query(Producto.class).optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Producto no encontrado"));
            if(!p.activo() || p.stock()<item.cantidad())
                throw new ResponseStatusException(CONFLICT,"Stock insuficiente para " + p.nombre());
            db.sql("UPDATE productos SET stock=stock-:cantidad WHERE id=:id").param("cantidad",item.cantidad()).param("id",item.productoId()).update();
            db.sql("INSERT INTO reserva_items VALUES (:orden,:producto,:nombre,:cantidad,:precio)")
                .param("orden",orden).param("producto",p.id()).param("nombre",p.nombre()).param("cantidad",item.cantidad()).param("precio",p.precio()).update();
        }
        return reservados(orden);
    }
    private List<Reservado> reservados(UUID id) {
        return db.sql("SELECT producto_id,nombre,cantidad,precio FROM reserva_items WHERE orden_id=:id ORDER BY producto_id")
            .param("id",id).query(Reservado.class).list();
    }
    public void liberar(UUID orden) {
        var state = db.sql("SELECT estado FROM reservas WHERE orden_id=:id FOR UPDATE").param("id",orden)
            .query(String.class).optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Reserva no encontrada"));
        if(state.equals("LIBERADA")) return;
        for(var item:reservados(orden))
            db.sql("UPDATE productos SET stock=stock+:cantidad WHERE id=:id").param("cantidad",item.cantidad()).param("id",item.productoId()).update();
        db.sql("UPDATE reservas SET estado='LIBERADA' WHERE orden_id=:id").param("id",orden).update();
    }
}
