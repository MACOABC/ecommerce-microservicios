package com.mercado.envios;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import static com.mercado.envios.EnvioService.*;

@RestController
class EnvioController {
    private final EnvioService service;
    EnvioController(EnvioService service) { this.service=service; }
    @PostMapping("/internal/envios") Envio crear(@Valid @RequestBody Solicitud s) { return service.crear(s); }
    @GetMapping("/api/envios/seguimiento/{guia}") Seguimiento buscar(@PathVariable String guia) { return service.seguimiento(guia); }
    @PatchMapping("/api/envios/{id}/estado") Envio cambiar(@PathVariable UUID id,@Valid @RequestBody Cambio c) { return service.cambiar(id,c); }
}
