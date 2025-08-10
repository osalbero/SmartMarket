package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Proveedor;
import com.smartmarket.api.services.ProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // Obtener todos los proveedores
    @GetMapping
    public ResponseEntity<List<Proveedor>> obtenerTodos() {
        return ResponseEntity.ok(proveedorService.obtenerTodos());
    }

    // Obtener un proveedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtenerPorId(@PathVariable Integer id) {
        Optional<Proveedor> proveedor = proveedorService.obtenerPorId(id);
        return proveedor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscar proveedores por nombre
    @GetMapping("/buscar")
    public List<Proveedor> buscarProveedores(@RequestParam String query) {
        return proveedorService.buscarPorNombreODireccion(query);
    }

    // Crear un nuevo proveedor
    @PostMapping
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        Proveedor nuevoProveedor = proveedorService.crearProveedor(proveedor);
        return ResponseEntity.ok(nuevoProveedor);
    }

    // Crear múltiples proveedores (Bulk Insert)
    @PostMapping("/lote")

    public ResponseEntity<List<Proveedor>> crearProveedores(@RequestBody List<Proveedor> proveedores) {
        List<Proveedor> nuevosProveedores = proveedorService.crearProveedores(proveedores);
        return ResponseEntity.ok(nuevosProveedores);
    }

    // Actualizar un proveedor existente
    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Integer id, @RequestBody Proveedor proveedor) {
        Proveedor actualizado = proveedorService.actualizarProveedor(id, proveedor);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un proveedor por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Integer id) {
        proveedorService.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}