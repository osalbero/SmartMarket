package com.smartmarket.api.controllers;

import com.smartmarket.api.models.*;
import com.smartmarket.api.services.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/gestion-stock")
public class GestionStockController {

    private final GestionStockService gestionStockService;

    // Constructor para inyección de dependencias
    public GestionStockController(GestionStockService gestionStockService) {
        this.gestionStockService = gestionStockService;
    }

    // Registrar un movimiento (vía parámetros)
    @PostMapping("/registrar")
    public ResponseEntity<GestionStock> registrarMovimiento(@RequestParam String sku,
                                                             @RequestParam int cantidad,
                                                             @RequestParam EstadoStock estadoStock,
                                                             @RequestParam String usuario,
                                                             @RequestParam String descripcion) {
        GestionStock nuevoMovimiento = gestionStockService.registrarMovimiento(sku, cantidad, estadoStock, usuario, descripcion);
        return ResponseEntity.ok(nuevoMovimiento);
    }

    // Registrar un movimiento (vía JSON)
    @PostMapping
    public ResponseEntity<?> registrarGestion(@RequestBody GestionStock gestionStock) {
        gestionStockService.registrarMovimiento(
                gestionStock.getSku(),
                gestionStock.getCantidad(),
                gestionStock.getEstadoStock(),
                gestionStock.getUsuarioResponsable(),
                gestionStock.getDescripcionMovimiento()
        );

        return ResponseEntity.ok("Movimiento de stock registrado y stock actualizado.");
    }

    // Consultar movimientos por SKU
    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<GestionStock>> obtenerMovimientosPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(gestionStockService.obtenerMovimientosPorSku(sku));
    }

    @GetMapping
    public ResponseEntity<List<GestionStock>> obtenerTodosMovimientos() {
        return ResponseEntity.ok(gestionStockService.obtenerTodosMovimientos());
    }
}