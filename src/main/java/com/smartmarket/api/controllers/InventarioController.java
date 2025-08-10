package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Inventario;
import com.smartmarket.api.models.MovimientoDeInventario;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.services.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    // Obtener lista completa del inventario
    @GetMapping
    public ResponseEntity<Inventario> obtenerInventario() {
        return ResponseEntity.ok(inventarioService.obtenerInventarioGeneral());
    }

    // Obtener movimientos de un producto por SKU
    @GetMapping("/api/movimientos/{sku}")
    public ResponseEntity<List<MovimientoDeInventario>> obtenerMovimientosPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(inventarioService.obtenerMovimientosPorSku(sku));
    }

    // Obtener detalles de un producto específico por SKU
    @GetMapping("/api/producto/{sku}")
    public ResponseEntity<Producto> obtenerDetallesProducto(@PathVariable String sku) {
        Optional<Producto> producto = inventarioService.obtenerDetallesProducto(sku);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}