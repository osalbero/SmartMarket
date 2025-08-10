package com.smartmarket.api.controllers;

import com.smartmarket.api.models.EstadoStock;
import com.smartmarket.api.models.InventarioProducto;
import com.smartmarket.api.services.InventarioProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class InventarioProductoController {
    
    private final InventarioProductoService inventarioProductoService;
    public InventarioProductoController(InventarioProductoService inventarioProductoService) {
        this.inventarioProductoService = inventarioProductoService;
    }

    @GetMapping("/sku/{sku}")
    public InventarioProducto consultarStock(@PathVariable String sku) {
        return inventarioProductoService.obtenerPorSku(sku);
    }

    @PostMapping("/mover-interno")
    public ResponseEntity<String> moverStockInterno(
            @RequestParam String sku,
            @RequestParam int cantidad,
            @RequestParam EstadoStock desde,
            @RequestParam EstadoStock hacia,
            @RequestParam String usuario) {
        inventarioProductoService.moverStockInterno(sku, cantidad, desde, hacia, usuario);
        return ResponseEntity.ok("Movimiento interno registrado correctamente.");
    }

    @PostMapping("/transferir-stock")
    public ResponseEntity<String> transferirStock(
            @RequestParam String sku,
            @RequestParam int cantidad,
            @RequestParam EstadoStock desde,
            @RequestParam EstadoStock hacia,
            @RequestParam String usuario) {
        inventarioProductoService.transferirEntreEstados(sku, cantidad, desde, hacia, usuario);
        return ResponseEntity.ok("Transferencia de stock registrada correctamente.");
    }

}
