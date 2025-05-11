package com.smartmarket.api.services;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {
    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    // Obtener todos los empleados
    public List<Empleado> obtenerTodos() {
        return empleadoRepository.findAll();
    }

    // Obtener un empleado por ID
    public Optional<Empleado> obtenerPorId(Integer id) {
        return empleadoRepository.findById(id);
    }

    // Crear un nuevo empleado
    public Empleado crearEmpleado(Empleado empleado) {
        if (empleadoRepository.existsByEmail(empleado.getEmail())) {
            throw new IllegalArgumentException("El empleado ya existe con ese email");
        }
        return empleadoRepository.save(empleado);
    }

    // Crear múltiples empleados (Bulk Insert)
    public List<Empleado> crearEmpleados(List<Empleado> empleados) {
        for (Empleado empleado : empleados) {
            if (empleadoRepository.existsByEmail(empleado.getEmail())) {
                throw new IllegalArgumentException("El empleado ya existe con ese email: " + empleado.getEmail());
            }
        }
        return empleadoRepository.saveAll(empleados);
    }

    // Actualizar un empleado existente
    public Empleado actualizarEmpleado(Integer id, Empleado empleadoActualizado) {
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setNombre(empleadoActualizado.getNombre());
                    empleado.setCargo(empleadoActualizado.getCargo());
                    empleado.setTelefono(empleadoActualizado.getTelefono());
                    empleado.setEmail(empleadoActualizado.getEmail());
                    return empleadoRepository.save(empleado);
                })
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    // Eliminar un empleado por ID
    public void eliminarEmpleado(Integer id) {
        if (!empleadoRepository.existsById(id)) {
            throw new IllegalArgumentException("El empleado no existe");
        }
        empleadoRepository.deleteById(id);
    }
}