package com.smartmarket.api.controllers;

import com.smartmarket.api.models.MovimientoDeInventario;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.services.MovimientoDeInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/movimientos-inventario")
public class MovimientoDeInventarioController {
    private final MovimientoDeInventarioService movimientoService;

    public MovimientoDeInventarioController(MovimientoDeInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    // Obtener todos los movimientos de inventario
    @GetMapping
    public ResponseEntity<List<MovimientoDeInventario>> obtenerTodos() {
        return ResponseEntity.ok(movimientoService.obtenerTodos());
    }

    // Obtener un movimiento por ID
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoDeInventario> obtenerPorId(@PathVariable Integer id) {
        Optional<MovimientoDeInventario> movimiento = movimientoService.obtenerPorId(id);
        return movimiento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscar productos por nombre, descripción o SKU
    @GetMapping("/buscar")
    public List<MovimientoDeInventario> buscarMovimientos(@RequestParam String query) {
        return movimientoService.buscarPorSkuNombre_Producto(query);
    }

    // Registrar un nuevo movimiento de inventario
    @PostMapping
    public ResponseEntity<MovimientoDeInventario> crearMovimiento(@RequestBody MovimientoDeInventario movimiento) {
        MovimientoDeInventario nuevoMovimiento = movimientoService.crearMovimiento(movimiento);
        return ResponseEntity.ok(nuevoMovimiento);
    }

    // Registrar un movimiento de inventario por lote
    @PostMapping("/lote")
    public ResponseEntity<?> guardarMovimientosEnLote(@RequestBody List<MovimientoDeInventario> movimientos) {
        movimientos.forEach(movimientoService::crearMovimiento);
        return ResponseEntity.ok("Lote registrado correctamente");
    }

    // Eliminar un movimiento por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Integer id) {
        movimientoService.eliminarMovimiento(id);
        return ResponseEntity.noContent().build();
    }
}