package com.smartmarket.api.services;

import com.smartmarket.api.models.Proveedor;
import com.smartmarket.api.repositories.IProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {
    private final IProveedorRepository proveedorRepository;

    public ProveedorService(IProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    // Obtener todos los proveedores
    public List<Proveedor> obtenerTodos() {
        return proveedorRepository.findAll();
    }

    // Obtener un proveedor por ID
    public Optional<Proveedor> obtenerPorId(Integer id) {
        return proveedorRepository.findById(id);
    }

    // Crear un nuevo proveedor
    public Proveedor crearProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByEmail(proveedor.getEmail())) {
            throw new IllegalArgumentException("El proveedor ya existe con ese email");
        }
        return proveedorRepository.save(proveedor);
    }

    // Crear múltiples proveedores (Bulk Insert)
    public List<Proveedor> crearProveedores(List<Proveedor> proveedores) {
        for (Proveedor proveedor : proveedores) {
            if (proveedorRepository.existsByEmail(proveedor.getEmail())) {
                throw new IllegalArgumentException("El proveedor ya existe con ese email: " + proveedor.getEmail());
            }
        }
        return proveedorRepository.saveAll(proveedores);
    }

    // Actualizar un proveedor existente
    public Proveedor actualizarProveedor(Integer id, Proveedor proveedorActualizado) {
        return proveedorRepository.findById(id)
                .map(proveedor -> {
                    proveedor.setNombre(proveedorActualizado.getNombre());
                    proveedor.setDireccion(proveedorActualizado.getDireccion());
                    proveedor.setTelefono(proveedorActualizado.getTelefono());
                    proveedor.setEmail(proveedorActualizado.getEmail());
                    return proveedorRepository.save(proveedor);
                })
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
    }

    // Eliminar un proveedor por ID
    public void eliminarProveedor(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("El proveedor no existe");
        }
        proveedorRepository.deleteById(id);
    }
}