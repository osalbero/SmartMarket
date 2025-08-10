package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import com.smartmarket.api.services.EmpleadoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*") // Permite cualquier origen
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoController(EmpleadoService empleadoService, IEmpleadoRepository empleadoRepository) {
        this.empleadoService = empleadoService;
        this.empleadoRepository = empleadoRepository;
    }

    // AÑADIDO TEMPORALMENTE: Endpoint para verificar la existencia del email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getEmpleadoByEmail(@PathVariable String email) {
        System.out.println("Endpoint de depuración: Buscando empleado con email -> " + email);
        Optional<Empleado> empleado = empleadoRepository.findByEmail(email);

        if (empleado.isPresent()) {
            System.out.println("Empleado encontrado: " + empleado.get().getNombre());
            // ⬅️ SOLUCIÓN: Devuelve solo el email como una cadena simple en lugar del objeto completo
            return ResponseEntity.ok("Empleado encontrado: " + empleado.get().getEmail());
        } else {
            System.out.println("Empleado NO encontrado en la base de datos.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empleado no encontrado con email: " + email);
        }
    }

    // Obtener todos los empleados
    @GetMapping
    public ResponseEntity<List<Empleado>> obtenerTodos() {
        List<Empleado> empleados = empleadoService.obtenerTodos();
        return ResponseEntity.ok(empleados);
    }

    // Obtener un empleado por ID
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerPorId(@PathVariable Integer id) {
        return empleadoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar empleados por nombre, cargo, teléfono o email
    // NOTA: El método se ha modificado para buscar por el nombre del cargo,
    // ya que la entidad Empleado ahora tiene un objeto Cargo y no un String.
    @GetMapping("/buscar")
    public ResponseEntity<List<Empleado>> buscarEmpleados(@RequestParam String query) {
        List<Empleado> empleados = empleadoService.buscarPorNombreOCargoOTelefonoOEmail(query);
        return ResponseEntity.ok(empleados);
    }

    // Crear un nuevo empleado
    // NOTA: El servicio debe manejar la lógica de encontrar el objeto Cargo
    // a partir del id_cargo que se podría recibir en el JSON.
    @PostMapping
    public ResponseEntity<Empleado> crearEmpleado(@RequestBody Empleado empleado) {
        Empleado nuevoEmpleado = empleadoService.crearEmpleadoConCargo(empleado);
        return new ResponseEntity<>(nuevoEmpleado, HttpStatus.CREATED);
    }

    // Crear múltiples empleados (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Empleado>> crearEmpleados(@RequestBody List<Empleado> empleados) {
        List<Empleado> nuevosEmpleados = empleadoService.crearEmpleados(empleados);
        return ResponseEntity.ok(nuevosEmpleados);
    }

    // Actualizar un empleado existente
    // NOTA: Similar al crear, el servicio debe gestionar la lógica del Cargo.
    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizarEmpleado(@PathVariable Integer id,
            @RequestBody Empleado empleadoActualizado) {
        Empleado empleado = empleadoService.actualizarEmpleado(id, empleadoActualizado);
        return ResponseEntity.ok(empleado);
    }

    // Eliminar un empleado por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Integer id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}