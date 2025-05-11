package com.smartmarket.api.controllers;

import com.smartmarket.api.models.GestionStock;
import com.smartmarket.api.models.EstadoStock;
import com.smartmarket.api.services.GestionStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestion-stock")
public class GestionStockController {
    private final GestionStockService gestionStockService;

    public GestionStockController(GestionStockService gestionStockService) {
        this.gestionStockService = gestionStockService;
    }

    // Registrar un movimiento de stock
    @PostMapping("/registrar")
    public ResponseEntity<GestionStock> registrarMovimiento(@RequestParam String sku,
                                                            @RequestParam int cantidad,
                                                            @RequestParam EstadoStock estadoStock,
                                                            @RequestParam String usuario) {
        GestionStock nuevoMovimiento = gestionStockService.registrarMovimiento(sku, cantidad, estadoStock, usuario);
        return ResponseEntity.ok(nuevoMovimiento);
    }

    // Consultar movimientos de un producto por SKU
    @GetMapping("/movimientos/{sku}")
    public ResponseEntity<List<GestionStock>> obtenerMovimientosPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(gestionStockService.obtenerMovimientosPorSku(sku));
    }
}