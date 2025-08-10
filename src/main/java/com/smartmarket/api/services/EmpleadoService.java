package com.smartmarket.api.services;

import com.smartmarket.api.models.Cargo;
import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.repositories.ICargoRepository;
import com.smartmarket.api.repositories.IEmpleadoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {
    private final IEmpleadoRepository empleadoRepository;
    private final ICargoRepository cargoRepository;

    public EmpleadoService(IEmpleadoRepository empleadoRepository, ICargoRepository cargoRepository) {
        this.empleadoRepository = empleadoRepository;
        this.cargoRepository = cargoRepository;
    }

    // Obtener todos los empleados
    public List<Empleado> obtenerTodos() {
        return empleadoRepository.findAll();
    }

    // Buscar empleados por nombre, cargo, teléfono o email
    public List<Empleado> buscarPorNombreOCargoOTelefonoOEmail(String query) {
        return empleadoRepository
                .findByNombreContainingIgnoreCaseOrCargoNombreContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query, query, query, query);
    }

    // Obtener un empleado por ID
    public Optional<Empleado> obtenerPorId(Integer id) {
        return empleadoRepository.findById(id);
    }

    // Crear un nuevo empleado
    public Empleado crearEmpleado(Empleado empleado) {
        // Validación: El email ya existe
        if (empleadoRepository.existsByEmail(empleado.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un empleado con el email: " + empleado.getEmail());
        }

        // Validación: El cargo debe existir
        Cargo cargoExistente = cargoRepository.findById(empleado.getCargo().getId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cargo especificado no existe."));
        empleado.setCargo(cargoExistente);

        return empleadoRepository.save(empleado);
    }

    // Crear un empleado con validación del cargo
    public Empleado crearEmpleadoConCargo(Empleado empleado) {
        // 1. Buscar cargo por ID
        Optional<Cargo> cargo = cargoRepository.findById(empleado.getCargo().getId());
        if (cargo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cargo '" + empleado.getCargo().getNombre() + "' no existe.");
        }

        // 2. Asignar el cargo y su nombre
        empleado.setCargo(cargo.get());
        empleado.setNombreCargo(cargo.get().getNombre());

        // 3. Guardar el empleado
        return empleadoRepository.save(empleado);
    }

    // Crear múltiples empleados (Bulk Insert)
    public List<Empleado> crearEmpleados(List<Empleado> empleados) {
        for (Empleado empleado : empleados) {
            // Validación: El email ya existe
            if (empleadoRepository.existsByEmail(empleado.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El empleado ya existe con el email: " + empleado.getEmail());
            }

            // Validación y asignación del cargo para cada empleado
            Cargo cargoExistente = cargoRepository.findById(empleado.getCargo().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Uno de los cargos especificados no existe."));
            empleado.setCargo(cargoExistente);
        }
        return empleadoRepository.saveAll(empleados);
    }

    // Actualizar un empleado existente
    public Empleado actualizarEmpleado(Integer id, Empleado empleadoActualizado) {
        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));

        // Validación del cargo
        if (empleadoActualizado.getCargo() == null || empleadoActualizado.getCargo().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe especificar un cargo válido");
        }

        Cargo cargo = cargoRepository.findById(empleadoActualizado.getCargo().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cargo con ID '" + empleadoActualizado.getCargo().getId() + "' no encontrado."));

        // Actualizar campos
        empleadoExistente.setCargo(cargo);
        empleadoExistente.setNombreCargo(cargo.getNombre());
        empleadoExistente.setNombre(empleadoActualizado.getNombre());
        empleadoExistente.setTelefono(empleadoActualizado.getTelefono());
        empleadoExistente.setEmail(empleadoActualizado.getEmail());

        return empleadoRepository.save(empleadoExistente);
    }

    // Eliminar un empleado por ID
    public void eliminarEmpleado(Integer id) {
        if (!empleadoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El empleado no existe");
        }
        empleadoRepository.deleteById(id);
    }

}
