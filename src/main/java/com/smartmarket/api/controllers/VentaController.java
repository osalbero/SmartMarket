package com.smartmarket.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmarket.api.models.Venta;
import com.smartmarket.api.services.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> datos) {
        Long Idcliente = Long.valueOf(datos.get("Idcliente").toString());
        Long Idempleado = Long.valueOf(datos.get("Idempleado").toString());
        List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.get("productos");

        return ventaService.registrarVenta(Idcliente, Idempleado, productos);
    }
    
    @GetMapping
    public ResponseEntity<List<Venta>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

}
