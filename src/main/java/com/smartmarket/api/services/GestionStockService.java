package com.smartmarket.api.services;

import com.smartmarket.api.models.GestionStock;
import com.smartmarket.api.models.EstadoStock;
import com.smartmarket.api.repositories.IGestionStockRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestionStockService {
    private final IGestionStockRepository gestionStockRepository;

    public GestionStockService(IGestionStockRepository gestionStockRepository) {
        this.gestionStockRepository = gestionStockRepository;
    }

    // Registrar un movimiento de stock
    public GestionStock registrarMovimiento(String sku, int cantidad, EstadoStock estadoStock, String usuario) {
        GestionStock movimiento = new GestionStock();
        movimiento.setSku(sku);
        movimiento.setCantidad(cantidad);
        movimiento.setEstadoStock(estadoStock);
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        return gestionStockRepository.save(movimiento);
    }

    // Consultar movimientos de stock por SKU
    public List<GestionStock> obtenerMovimientosPorSku(String sku) {
        return gestionStockRepository.findBySku(sku);
    }
}