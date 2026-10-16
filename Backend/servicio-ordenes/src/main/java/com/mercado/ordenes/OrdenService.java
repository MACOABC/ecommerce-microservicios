package com.mercado.ordenes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
public class OrdenService {
    public record Item(@NotNull @Positive Long productoId,@NotNull @Min(1) @Max(999) Integer cantidad) {}
    public record Compra(@NotNull UUID id,@NotBlank @Size(max=100) String cliente,
        @NotBlank @Email @Size(max=160) String email,@NotBlank @Size(max=240) String direccion,
        @NotNull Boolean aprobarPago,@NotEmpty @Size(max=100) List<@NotNull @Valid Item> items) {}
    public record Cabecera(UUID id,String cliente,String email,String direccion,Boolean aprobarPago,String estado,BigDecimal total,OffsetDateTime creada) {}
    public record Detalle(Long productoId,Integer cantidad,String nombre,BigDecimal precio) {}
    public record Orden(UUID id,String cliente,String email,String direccion,String estado,BigDecimal total,OffsetDateTime creada,List<Detalle> items) {}
    public record Reservado(Long productoId,String nombre,Integer cantidad,BigDecimal precio) {}
    private final JdbcClient db;
    private final TransactionTemplate tx;
    private final RestClient productos;
    private final RestClient envios;
    public OrdenService(JdbcClient db,TransactionTemplate tx,RestClient.Builder builder,
        @Value("${app.productos-url}") String productosUrl,@Value("${app.envios-url}") String enviosUrl) {
        this.db=db; this.tx=tx; this.productos=builder.clone().baseUrl(productosUrl).build();
        this.envios=builder.clone().baseUrl(enviosUrl).build();
    }
    public Orden buscar(UUID id) { return vista(cabecera(id)); }
    private Cabecera cabecera(UUID id) {
        return db.sql("SELECT * FROM ordenes WHERE id=:id").param("id",id).query(Cabecera.class)
            .optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Orden no encontrada"));
    }
    private Orden vista(Cabecera o) {
        var items=db.sql("SELECT producto_id,cantidad,nombre,precio FROM orden_items WHERE orden_id=:id ORDER BY producto_id")
            .param("id",o.id()).query(Detalle.class).list();
        return new Orden(o.id(),o.cliente(),o.email(),o.direccion(),o.estado(),o.total(),o.creada(),items);
    }
    public Orden comprar(Compra c) {
        var items=c.items().stream().sorted(Comparator.comparing(Item::productoId)).toList();
        if(items.stream().map(Item::productoId).distinct().count()!=items.size())
            throw new ResponseStatusException(BAD_REQUEST,"No repitas productos en la compra");
        // Primera transacción: persiste la intención antes de llamar a otro servicio.
        tx.executeWithoutResult(status -> {
            int created=db.sql("""
                INSERT INTO ordenes(id,cliente,email,direccion,aprobar_pago) VALUES (:id,:cliente,:email,:direccion,:aprobar)
                ON CONFLICT DO NOTHING
                """).param("id",c.id()).param("cliente",c.cliente().trim()).param("email",c.email().trim())
                .param("direccion",c.direccion().trim()).param("aprobar",c.aprobarPago()).update();
            if(created>0) {
                for(var i:items) db.sql("INSERT INTO orden_items(orden_id,producto_id,cantidad) VALUES (:id,:producto,:cantidad)")
                    .param("id",c.id()).param("producto",i.productoId()).param("cantidad",i.cantidad()).update();
            } else {
                var o=buscar(c.id());
                var existing=o.items().stream().map(i -> new Item(i.productoId(),i.cantidad())).toList();
                if(!o.cliente().equals(c.cliente().trim()) || !o.email().equals(c.email().trim()) ||
                   !o.direccion().equals(c.direccion().trim()) || !cabecera(c.id()).aprobarPago().equals(c.aprobarPago()) || !existing.equals(items))
                    throw new ResponseStatusException(CONFLICT,"El identificador de compra ya se usó con otros datos");
            }
        });
        return procesar(c.id());
    }
    public Orden procesar(UUID id) {
        // El bloqueo serializa pagos repetidos. Un fallo remoto revierte sólo esta transacción.
        // La reserva remota usa el mismo UUID y puede recuperarse en el siguiente intento.
        return tx.execute(status -> {
            var o=db.sql("SELECT * FROM ordenes WHERE id=:id FOR UPDATE").param("id",id).query(Cabecera.class)
                .optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Orden no encontrada"));
            if(!o.estado().equals("CREADA")) return vista(o);
            var items=vista(o).items().stream().map(i -> new Item(i.productoId(),i.cantidad())).toList();
            var reserved=productos.post().uri("/internal/reservas/{id}",id).body(Map.of("items",items))
                .retrieve().body(new ParameterizedTypeReference<List<Reservado>>() {});
            if(reserved == null || reserved.size()!=items.size()) throw new ResponseStatusException(BAD_GATEWAY,"Reserva remota incompleta");
            BigDecimal total=BigDecimal.ZERO;
            for(var r:reserved) {
                total=total.add(r.precio().multiply(BigDecimal.valueOf(r.cantidad())));
                db.sql("UPDATE orden_items SET nombre=:nombre,precio=:precio WHERE orden_id=:id AND producto_id=:producto")
                    .param("nombre",r.nombre()).param("precio",r.precio()).param("id",id).param("producto",r.productoId()).update();
            }
            // Simulación explícita: no recibe ni almacena datos bancarios.
            if(!o.aprobarPago()) productos.post().uri("/internal/reservas/{id}/liberar",id).retrieve().toBodilessEntity();
            db.sql("UPDATE ordenes SET estado=:estado,total=:total WHERE id=:id").param("estado",o.aprobarPago() ? "PAGADA" : "RECHAZADA")
                .param("total",total).param("id",id).update();
            return buscar(id);
        });
    }
    public Map<String,Object> crearEnvio(UUID id) {
        if(!cabecera(id).estado().equals("PAGADA")) throw new ResponseStatusException(CONFLICT,"Sólo se envían órdenes pagadas");
        var result=envios.post().uri("/internal/envios").body(Map.of("ordenId",id))
            .retrieve().body(new ParameterizedTypeReference<Map<String,Object>>() {});
        if(result==null) throw new ResponseStatusException(BAD_GATEWAY,"Respuesta vacía de logística");
        return result;
    }
}
