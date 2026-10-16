package com.mercado.ordenes;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import static com.mercado.ordenes.OrdenService.*;

@RestController
@RequestMapping("/api/ordenes")
class OrdenController {
    private final OrdenService service;
    OrdenController(OrdenService service) { this.service=service; }
    @PostMapping Orden comprar(@Valid @RequestBody Compra c) { return service.comprar(c); }
    @GetMapping("/{id}") Orden buscar(@PathVariable UUID id) { return service.buscar(id); }
    @PostMapping("/{id}/reintentar") Orden reintentar(@PathVariable UUID id) { return service.procesar(id); }
    @PostMapping("/{id}/envio") Map<String,Object> envio(@PathVariable UUID id) { return service.crearEnvio(id); }
}
