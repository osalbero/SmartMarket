package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Producto;
import com.smartmarket.api.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    // Obtener un producto por SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Producto> obtenerPorSku(@PathVariable String sku) {
        Optional<Producto> producto = productoService.obtenerPorSku(sku);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.obtenerPorId(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo producto
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.crearProductoConCategoria(producto);
        return ResponseEntity.ok(nuevoProducto);
    }

    // Crear múltiples productos (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Producto>> crearProductosEnLote(@RequestBody List<Producto> productos) {
        List<Producto> nuevosProductos = productoService.crearProductosEnLote(productos);
        return ResponseEntity.ok(nuevosProductos);
    }

    // Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(actualizado);
    }

    // Actualizar un producto por SKU
    @PutMapping("/sku/{sku}")
    public ResponseEntity<Producto> actualizarPorSku(@PathVariable String sku, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizarProductoPorSku(sku, producto);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un producto por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}