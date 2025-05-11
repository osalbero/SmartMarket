package com.smartmarket.api.services;

import com.smartmarket.api.models.Inventario;
import com.smartmarket.api.models.MovimientoDeInventario;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.repositories.IInventarioRepository;
import com.smartmarket.api.repositories.IProductoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventarioService {
    private final IInventarioRepository inventarioRepository;
    private final IProductoRepository productoRepository;

    public InventarioService(IInventarioRepository inventarioRepository, IProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository; // ✅ Inyectado correctamente
    }


    // Obtener vista general del inventario
    public Inventario obtenerInventarioGeneral() {
        List<Producto> productos = inventarioRepository.obtenerInventarioGeneral();
        List<MovimientoDeInventario> movimientos = inventarioRepository.obtenerMovimientosPorSku(null);
        return new Inventario(productos, movimientos);
    }

    // Consultar movimientos de un producto por SKU
    public List<MovimientoDeInventario> obtenerMovimientosPorSku(String sku) {
        return inventarioRepository.obtenerMovimientosPorSku(sku);
    }
    
    // Consultar detalles de un producto específico por SKU
    public Optional<Producto> obtenerDetallesProducto(String sku) {
        return productoRepository.findBySku(sku);
    }
}