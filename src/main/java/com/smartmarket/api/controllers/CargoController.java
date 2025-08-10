package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Cargo;
import com.smartmarket.api.services.CargoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/cargos")
public class CargoController {

    private final CargoService cargoService;

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    // Obtener todos los cargos
    @GetMapping
    public ResponseEntity<List<Cargo>> obtenerTodos() {
        return ResponseEntity.ok(cargoService.obtenerTodos());
    }

    // Obtener un cargo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cargo> obtenerPorId(@PathVariable Integer id) {
        return cargoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    // Buscar cargos por nombre
    @GetMapping("/buscar")
    public List<Cargo> buscarCargos(@RequestParam String query) {
        return cargoService.buscarPorNombre(query);
    }

    // Crear un nuevo cargo
    @PostMapping
    public ResponseEntity<Cargo> crearCargo(@RequestBody Cargo cargo) {
        Cargo nuevoCargo = cargoService.crearCargo(cargo.getNombre());
        return ResponseEntity.ok(nuevoCargo);
    }

    // Crear múltiples cargos (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Cargo>> crearCargos(@RequestBody List<Cargo> cargos) {
        List<Cargo> nuevosCargos = cargoService.crearCargos(cargos);
        return ResponseEntity.ok(nuevosCargos);
    }

    // Eliminar un cargo por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCargo(@PathVariable Integer id) {
        cargoService.eliminarCargo(id);
        return ResponseEntity.noContent().build();
    }

    // Actualizar un cargo por ID
    @PutMapping("/{id}")
    public ResponseEntity<Cargo> actualizarCargo(@PathVariable Integer id, @RequestBody Cargo cargo) {
        Cargo actualizado = cargoService.actualizarCargo(id, cargo);
        return ResponseEntity.ok(actualizado);
    }
}