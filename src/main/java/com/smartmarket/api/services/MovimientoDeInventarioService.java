package com.smartmarket.api.services;

import com.smartmarket.api.models.MovimientoDeInventario;
import com.smartmarket.api.repositories.IMovimientoDeInventarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovimientoDeInventarioService {
    private final IMovimientoDeInventarioRepository movimientoRepository;

    public MovimientoDeInventarioService(IMovimientoDeInventarioRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    // Obtener todos los movimientos de inventario
    public List<MovimientoDeInventario> obtenerTodos() {
        return movimientoRepository.findAll();
    }

    // Obtener un movimiento por ID
    public Optional<MovimientoDeInventario> obtenerPorId(Integer id) {
        return movimientoRepository.findById(id);
    }

    // Registrar un nuevo movimiento
    public MovimientoDeInventario crearMovimiento(MovimientoDeInventario movimiento) {
        return movimientoRepository.save(movimiento);
    }

    // Eliminar un movimiento por ID
    public void eliminarMovimiento(Integer id) {
        if (!movimientoRepository.existsById(id)) {
            throw new IllegalArgumentException("El movimiento de inventario no existe");
        }
        movimientoRepository.deleteById(id);
    }
}