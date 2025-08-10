package com.smartmarket.api.controllers;

import com.smartmarket.api.models.HistoricoPrecio;
import com.smartmarket.api.services.HistoricoPrecioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/historico-precio")
@CrossOrigin(origins = "*")
public class HistoricoPrecioController {

    @Autowired
    private HistoricoPrecioService historicoPrecioService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return historicoPrecioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return historicoPrecioService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody HistoricoPrecio historicoPrecio) {
        return historicoPrecioService.crear(historicoPrecio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody HistoricoPrecio historicoPrecio) {
        return historicoPrecioService.actualizar(id, historicoPrecio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        return historicoPrecioService.eliminar(id);
    }
}
