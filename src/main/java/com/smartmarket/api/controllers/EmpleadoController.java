package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.services.EmpleadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public ResponseEntity<List<Empleado>> obtenerTodos() {
        return ResponseEntity.ok(empleadoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerPorId(@PathVariable Integer id) {
        Optional<Empleado> empleado = empleadoService.obtenerPorId(id);
        return empleado.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Empleado> crearEmpleado(@RequestBody Empleado empleado) {
        return ResponseEntity.ok(empleadoService.crearEmpleado(empleado));
    }

    // Crear múltiples empleados (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Empleado>> crearEmpleados(@RequestBody List<Empleado> empleados) {
        return ResponseEntity.ok(empleadoService.crearEmpleados(empleados));
    }

    // Actualizar un empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizarEmpleado(@PathVariable Integer id, @RequestBody Empleado empleado) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, empleado));
    }

    // Eliminar un empleado por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Integer id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}