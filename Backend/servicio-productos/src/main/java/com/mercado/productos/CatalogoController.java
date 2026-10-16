package com.mercado.productos;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.*;
import static com.mercado.productos.CatalogoService.*;

@RestController
class CatalogoController {
    private final CatalogoService service;
    CatalogoController(CatalogoService service) { this.service=service; }
    @GetMapping("/api/categorias") List<Categoria> categorias() { return service.categorias(); }
    @PostMapping("/api/categorias") @ResponseStatus(CREATED)
    Categoria crearCategoria(@Valid @RequestBody CategoriaInput input) { return service.guardarCategoria(null,input); }
    @PutMapping("/api/categorias/{id}")
    Categoria actualizarCategoria(@PathVariable long id,@Valid @RequestBody CategoriaInput input) { return service.guardarCategoria(id,input); }
    @DeleteMapping("/api/categorias/{id}") @ResponseStatus(NO_CONTENT)
    void borrarCategoria(@PathVariable long id) { service.borrarCategoria(id); }
    @GetMapping("/api/productos")
    List<Producto> productos(@RequestParam(defaultValue="") String q,@RequestParam(required=false) Long categoria) { return service.productos(q,categoria); }
    @GetMapping("/api/productos/{id}") Producto producto(@PathVariable long id) { return service.producto(id); }
    @PostMapping("/api/productos") @ResponseStatus(CREATED)
    Producto crear(@Valid @RequestBody ProductoInput input) { return service.guardar(null,input); }
    @PutMapping("/api/productos/{id}")
    Producto actualizar(@PathVariable long id,@Valid @RequestBody ProductoInput input) { return service.guardar(id,input); }
    @DeleteMapping("/api/productos/{id}") @ResponseStatus(NO_CONTENT)
    void borrar(@PathVariable long id) { service.borrar(id); }
    // Sólo accesibles en el servicio de productos; el gateway no publica /internal.
    @PostMapping("/internal/reservas/{id}")
    List<Reservado> reservar(@PathVariable UUID id,@Valid @RequestBody ReservaInput input) { return service.reservar(id,input); }
    @PostMapping("/internal/reservas/{id}/liberar") @ResponseStatus(NO_CONTENT)
    void liberar(@PathVariable UUID id) { service.liberar(id); }
}
