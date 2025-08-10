package com.smartmarket.api.services;

import com.smartmarket.api.models.Cargo;
import com.smartmarket.api.repositories.ICargoRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CargoService {
    private final ICargoRepository cargoRepository;

    public CargoService(ICargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    // Crear múltiples cargos
    public List<Cargo> crearCargos(List<Cargo> cargos) {
        for (Cargo cargo : cargos) {
            if (cargoRepository.existsByNombreIgnoreCase(cargo.getNombre())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El cargo " + cargo.getNombre() + " ya existe");
            }
        }
        return cargoRepository.saveAll(cargos);
    }

    // Obtener todos los cargos
    public List<Cargo> obtenerTodos() {
        return cargoRepository.findAll();
    }

    // Buscar por nombre
    public List<Cargo> buscarPorNombre(String query) {
        return cargoRepository.findByNombreContainingIgnoreCase(query);
    }

    // Buscar por ID
    public Optional<Cargo> obtenerPorId(Integer id) {
        return cargoRepository.findById(id);
    }

    // Crear nuevo cargo si no existe
    public Cargo crearCargo(String nombre) {
        if (cargoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cargo '" + nombre + "' ya existe");
        }
        Cargo nuevoCargo = new Cargo();
        nuevoCargo.setNombre(nombre);
        return cargoRepository.save(nuevoCargo);
    }

    // Eliminar cargo
    public void eliminarCargo(Integer id) {
        if (!cargoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El cargo no existe");
        }

        try {
            cargoRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar el cargo porque tiene productos asociados."
            );
        }
    }

    // Actualizar cargo con validación de duplicado
    public Cargo actualizarCargo(Integer id, Cargo cargo) {
        Cargo existente = cargoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El cargo no existe"));

        // Verificar que no se esté duplicando el nombre (con otro ID)
        Optional<Cargo> otraConMismoNombre = cargoRepository.findByNombreIgnoreCase(cargo.getNombre());
        if (otraConMismoNombre.isPresent() && !otraConMismoNombre.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro cargo con el nombre '" + cargo.getNombre() + "'");
        }

        existente.setNombre(cargo.getNombre());
        return cargoRepository.save(existente);
    }
}
