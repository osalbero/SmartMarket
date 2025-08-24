package com.smartmarket.api.services;

import com.smartmarket.api.models.EstadoStock;
import com.smartmarket.api.models.GestionStock;
import com.smartmarket.api.models.InventarioProducto;
import com.smartmarket.api.models.InventarioProductoDTO;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.repositories.IGestionStockRepository;
import com.smartmarket.api.repositories.IProductoRepository;
import com.smartmarket.api.repositories.InventarioProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioProductoService {

    @Autowired
    private InventarioProductoRepository inventarioRepo;
    @Autowired
    private IProductoRepository productoRepo;

    @Autowired
    private IGestionStockRepository gestionStockRepo;

    public List<InventarioProducto> listarInventario() {
        return inventarioRepo.findAll();
    }

    public void actualizarStockDesdeGestion(GestionStock movimiento) {
        InventarioProducto inv = inventarioRepo.findBySku(movimiento.getSku())
                .orElseGet(() -> crearNuevoInventario(movimiento.getSku()));

        switch (movimiento.getEstadoStock()) {
            case DISPONIBLE -> inv.setStockDisponible(inv.getStockDisponible() + movimiento.getCantidad());
            case BLOQUEADO -> inv.setStockBloqueado(inv.getStockBloqueado() + movimiento.getCantidad());
            case AGOTADO -> inv.setStockAgotado(inv.getStockAgotado() + movimiento.getCantidad());
        }

        if (inv.getStockDisponible() > 0) {
            inv.setEstadoStockActual(EstadoStock.DISPONIBLE);
        } else if (inv.getStockBloqueado() > 0) {
            inv.setEstadoStockActual(EstadoStock.BLOQUEADO);
        } else {
            inv.setEstadoStockActual(EstadoStock.AGOTADO);
        }
        inv.setUltimaActualizacion(LocalDateTime.now());

        inventarioRepo.save(inv);
    }

    public void moverStockInterno(String sku, int cantidad, EstadoStock desde, EstadoStock hacia, String usuario) {
        InventarioProducto inventario = inventarioRepo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("SKU no encontrado: " + sku));

        // Verificación y resta del stock en el estado origen
        switch (desde) {
            case DISPONIBLE -> {
                if (inventario.getStockDisponible() < cantidad) {
                    throw new IllegalArgumentException("Stock DISPONIBLE insuficiente");
                }
                inventario.setStockDisponible(inventario.getStockDisponible() - cantidad);
            }
            case BLOQUEADO -> {
                if (inventario.getStockBloqueado() < cantidad) {
                    throw new IllegalArgumentException("Stock BLOQUEADO insuficiente");
                }
                inventario.setStockBloqueado(inventario.getStockBloqueado() - cantidad);
            }
            case AGOTADO -> {
                if (inventario.getStockAgotado() < cantidad) {
                    throw new IllegalArgumentException("Stock AGOTADO insuficiente");
                }
                inventario.setStockAgotado(inventario.getStockAgotado() - cantidad);
            }
        }

        // Sumar al estado destino
        switch (hacia) {
            case DISPONIBLE -> inventario.setStockDisponible(inventario.getStockDisponible() + cantidad);
            case BLOQUEADO -> inventario.setStockBloqueado(inventario.getStockBloqueado() + cantidad);
            case AGOTADO -> inventario.setStockAgotado(inventario.getStockAgotado() + cantidad);
        }

        inventario.setEstadoStockActual(hacia);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepo.save(inventario);

        // Registrar movimiento en la tabla gestion_stock para trazabilidad
        GestionStock registro = new GestionStock();
        registro.setSku(sku);
        registro.setCantidad(cantidad);
        registro.setEstadoStock(hacia);
        registro.setUsuarioResponsable(usuario); // o "Sistema" si es automático
        registro.setFechaMovimiento(LocalDateTime.now());
        gestionStockRepo.save(registro);
    }

    private InventarioProducto crearNuevoInventario(String sku) {
        InventarioProducto nuevo = new InventarioProducto();
        nuevo.setSku(sku);
        nuevo.setStockDisponible(0);
        nuevo.setStockBloqueado(0);
        nuevo.setStockAgotado(0);
        nuevo.setEstadoStockActual(EstadoStock.DISPONIBLE);
        nuevo.setUltimaActualizacion(LocalDateTime.now());
        return inventarioRepo.save(nuevo); // 💾 Guardar antes de retornar
    }

    public InventarioProducto obtenerPorSku(String sku) {
        return inventarioRepo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en inventario con SKU: " + sku));
    }

    public void transferirEntreEstados(String sku, int cantidad, EstadoStock desde, EstadoStock hacia, String usuario) {
        InventarioProducto inv = inventarioRepo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado para SKU: " + sku));

        // Verificar si hay stock suficiente en el estado origen
        int stockDisponible = switch (desde) {
            case DISPONIBLE -> inv.getStockDisponible();
            case BLOQUEADO -> inv.getStockBloqueado();
            case AGOTADO -> inv.getStockAgotado();
        };

        if (stockDisponible < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock en el estado " + desde + " para mover.");
        }

        // Restar del estado origen
        switch (desde) {
            case DISPONIBLE -> inv.setStockDisponible(inv.getStockDisponible() - cantidad);
            case BLOQUEADO -> inv.setStockBloqueado(inv.getStockBloqueado() - cantidad);
            case AGOTADO -> inv.setStockAgotado(inv.getStockAgotado() - cantidad);
        }

        // Sumar al estado destino
        switch (hacia) {
            case DISPONIBLE -> inv.setStockDisponible(inv.getStockDisponible() + cantidad);
            case BLOQUEADO -> inv.setStockBloqueado(inv.getStockBloqueado() + cantidad);
            case AGOTADO -> inv.setStockAgotado(inv.getStockAgotado() + cantidad);
        }

        // Actualizar el estado actual del inventario
        if (inv.getStockDisponible() > 0) {
            inv.setEstadoStockActual(EstadoStock.DISPONIBLE);
        } else if (inv.getStockBloqueado() > 0) {
            inv.setEstadoStockActual(EstadoStock.BLOQUEADO);
        } else {
            inv.setEstadoStockActual(EstadoStock.AGOTADO);
        }

        inv.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepo.save(inv);

        String descripcionMovimiento = "Transferencia de " + desde.name() + " a " + hacia.name();

        // Registrar en la tabla de gestión
        GestionStock gestion = new GestionStock();
        gestion.setSku(sku);
        gestion.setCantidad(cantidad);
        gestion.setEstadoStock(hacia);
        gestion.setUsuarioResponsable(usuario);
        gestion.setFechaMovimiento(LocalDateTime.now());
        gestion.setDescripcionMovimiento(descripcionMovimiento);
        gestionStockRepo.save(gestion);
    }

    public List<InventarioProductoDTO> listarInventarioConNombre() {
    List<InventarioProducto> inventarios = inventarioRepo.findAll();
    List<InventarioProductoDTO> resultado = new ArrayList<>();

    for (InventarioProducto inv : inventarios) {
        InventarioProductoDTO dto = new InventarioProductoDTO();
        dto.setSku(inv.getSku());
        dto.setStockDisponible(inv.getStockDisponible());
        dto.setStockBloqueado(inv.getStockBloqueado());
        dto.setStockAgotado(inv.getStockAgotado());
        dto.setEstadoStockActual(inv.getEstadoStockActual());
        dto.setUltimaActualizacion(inv.getUltimaActualizacion());

        // Buscar el nombre del producto por SKU
        Optional<Producto> producto = productoRepo.findBySku(inv.getSku());
        dto.setNombre(producto.map(Producto::getNombre).orElse("Sin nombre"));

        resultado.add(dto);
    }

    return resultado;
}

    public List<InventarioProductoDTO> buscarPorNombreOSku(String query) {
    List<InventarioProducto> inventarios = inventarioRepo.findAll();
    List<InventarioProductoDTO> resultado = new ArrayList<>();

    for (InventarioProducto inv : inventarios) {
        Optional<Producto> producto = productoRepo.findBySku(inv.getSku());

        String nombre = producto.map(Producto::getNombre).orElse("Sin nombre");
        boolean coincide = inv.getSku().toLowerCase().contains(query.toLowerCase()) ||
                           nombre.toLowerCase().contains(query.toLowerCase());

        if (coincide) {
            InventarioProductoDTO dto = new InventarioProductoDTO();
            dto.setSku(inv.getSku());
            dto.setNombre(nombre);
            dto.setStockDisponible(inv.getStockDisponible());
            dto.setStockBloqueado(inv.getStockBloqueado());
            dto.setStockAgotado(inv.getStockAgotado());
            dto.setEstadoStockActual(inv.getEstadoStockActual());
            dto.setUltimaActualizacion(inv.getUltimaActualizacion());
            resultado.add(dto);
        }
    }

    return resultado;
}



}
