package com.smartmarket.api.services;

import com.smartmarket.api.models.EstadoStock;
import com.smartmarket.api.models.GestionStock;
import com.smartmarket.api.repositories.IGestionStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestionStockService {

    @Autowired
    private IGestionStockRepository gestionStockRepo;

    @Autowired
    private InventarioProductoService inventarioProductoService;

    public GestionStock registrarMovimiento(String sku, int cantidad, EstadoStock estadoStock, String usuario, String descripcion) {
        GestionStock movimiento = new GestionStock();
        movimiento.setSku(sku);
        movimiento.setCantidad(cantidad);
        movimiento.setEstadoStock(estadoStock);
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setDescripcionMovimiento(descripcion);

        System.out.println("🟢 Registrando en gestion_stock: SKU=" + sku + ", cantidad=" + cantidad + ", estado=" + estadoStock);

        // Guardar el movimiento
        gestionStockRepo.save(movimiento);

        // Actualizar inventario
        inventarioProductoService.actualizarStockDesdeGestion(movimiento);

        return movimiento;
    }

    public List<GestionStock> obtenerMovimientosPorSku(String sku) {
        return gestionStockRepo.findBySku(sku);
    }

    public List<GestionStock> obtenerTodosMovimientos() {
        return gestionStockRepo.findAll();
    }
}
