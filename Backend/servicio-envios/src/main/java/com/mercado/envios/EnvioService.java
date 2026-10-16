package com.mercado.envios;

import java.time.OffsetDateTime;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class EnvioService {
    public record Solicitud(@NotNull UUID ordenId) {}
    public enum Estado { PREPARANDO, DESPACHADO, EN_TRANSITO, ENTREGADO }
    public record Cambio(@NotNull Estado estado) {}
    public record OrdenRemota(UUID id,String cliente,String direccion,String estado) {}
    public record Envio(UUID id,UUID ordenId,String guia,String destinatario,String direccion,Estado estado,OffsetDateTime creado,OffsetDateTime actualizado) {}
    public record Evento(String estado,OffsetDateTime fecha) {}
    public record Seguimiento(Envio envio,List<Evento> eventos) {}
    private final JdbcClient db;
    private final RestClient ordenes;
    public EnvioService(JdbcClient db,RestClient.Builder builder,@Value("${app.ordenes-url}") String url) {
        this.db=db; this.ordenes=builder.baseUrl(url).build();
    }
    public Envio crear(Solicitud input) {
        var existing=db.sql("SELECT * FROM envios WHERE orden_id=:id").param("id",input.ordenId()).query(Envio.class).optional();
        if(existing.isPresent()) return existing.get();
        var o=ordenes.get().uri("/api/ordenes/{id}",input.ordenId()).retrieve().body(OrdenRemota.class);
        if(o==null || !"PAGADA".equals(o.estado())) throw new ResponseStatusException(CONFLICT,"La orden no está pagada");
        UUID id=UUID.randomUUID();
        int created=db.sql("""
            INSERT INTO envios(id,orden_id,guia,destinatario,direccion,estado)
            VALUES (:id,:orden,:guia,:cliente,:direccion,'PREPARANDO') ON CONFLICT (orden_id) DO NOTHING
            """).param("id",id).param("orden",o.id()).param("guia","ENV-"+id.toString().toUpperCase(Locale.ROOT))
            .param("cliente",o.cliente()).param("direccion",o.direccion()).update();
        if(created>0) evento(id,Estado.PREPARANDO);
        return db.sql("SELECT * FROM envios WHERE orden_id=:id").param("id",input.ordenId()).query(Envio.class).single();
    }
    @Transactional(readOnly=true)
    public Seguimiento seguimiento(String guia) {
        var envio=db.sql("SELECT * FROM envios WHERE guia=:guia").param("guia",guia.toUpperCase(Locale.ROOT))
            .query(Envio.class).optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Guía no encontrada"));
        var eventos=db.sql("SELECT estado,fecha FROM envio_eventos WHERE envio_id=:id ORDER BY id")
            .param("id",envio.id()).query(Evento.class).list();
        return new Seguimiento(envio,eventos);
    }
    public Envio cambiar(UUID id,Cambio input) {
        var e=db.sql("SELECT * FROM envios WHERE id=:id FOR UPDATE").param("id",id).query(Envio.class)
            .optional().orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Envío no encontrado"));
        if(e.estado()==input.estado()) return e;
        if(input.estado().ordinal()!=e.estado().ordinal()+1) throw new ResponseStatusException(CONFLICT,"Avanza sólo al siguiente estado de entrega");
        var result=db.sql("UPDATE envios SET estado=:estado,actualizado=CURRENT_TIMESTAMP WHERE id=:id RETURNING *")
            .param("estado",input.estado().name()).param("id",id).query(Envio.class).single();
        evento(id,input.estado());
        return result;
    }
    private void evento(UUID id,Estado estado) {
        db.sql("INSERT INTO envio_eventos(envio_id,estado) VALUES (:id,:estado)").param("id",id).param("estado",estado.name()).update();
    }
}
