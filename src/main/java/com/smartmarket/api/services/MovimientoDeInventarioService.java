package com.smartmarket.api.services;

import com.smartmarket.api.models.*;
import com.smartmarket.api.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MovimientoDeInventarioService {

    private final IMovimientoDeInventarioRepository movimientoRepository;
    private final InventarioProductoRepository inventarioProductoRepository;
    private final IProveedorRepository proveedorRepository;
    private final IProductoRepository productoRepository;
    private final IEmpleadoRepository empleadoRepository;
    private final GestionStockService gestionStockService;

    public MovimientoDeInventarioService(IMovimientoDeInventarioRepository movimientoRepository,
            InventarioProductoRepository inventarioProductoRepository,
            IProveedorRepository proveedorRepository,
            IProductoRepository productoRepository,
            IEmpleadoRepository empleadoRepository,
            GestionStockService gestionStockService) {
        this.movimientoRepository = movimientoRepository;
        this.inventarioProductoRepository = inventarioProductoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.empleadoRepository = empleadoRepository;
        this.gestionStockService = gestionStockService;
    }

    // Obtener todos los movimientos de inventario
    public List<MovimientoDeInventario> obtenerTodos() {
        return movimientoRepository.findAll();
    }

    // Buscar movimientos de inventario por nombre, descripción o SKU
    public List<MovimientoDeInventario> buscarPorSkuNombre_Producto(String query) {
        return movimientoRepository.findBySkuContainingIgnoreCaseOrProducto_NombreContainingIgnoreCase(query, query);
    }

    // Obtener un movimiento por ID
    public Optional<MovimientoDeInventario> obtenerPorId(Integer id) {
        return movimientoRepository.findById(id);
    }

    // Registrar un nuevo movimiento y actualizar el inventario
    public MovimientoDeInventario crearMovimiento(MovimientoDeInventario movimiento) {

        // 1. Buscar producto por SKU
        String sku = movimiento.getSku();

        Producto producto = productoRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con SKU: " + sku));

        // Validar y obtener entidades persistentes desde la base de datos
        Proveedor proveedor = proveedorRepository.findById(movimiento.getProveedor().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proveedor no encontrado con ID: " + movimiento.getProveedor().getId()));

        Empleado empleado = empleadoRepository.findById(movimiento.getEmpleado().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empleado no encontrado con ID: " + movimiento.getEmpleado().getId()));

        // Asignar las entidades gestionadas (persistentes)
        movimiento.setProveedor(proveedor);
        movimiento.setProducto(producto);
        movimiento.setEmpleado(empleado);

        // Buscar o crear inventario del producto
        InventarioProducto inventario = inventarioProductoRepository.findBySku(sku)
                .orElseGet(() -> crearInventarioInicial(sku));

        // Actualizar stock según tipo de movimiento
        if (movimiento.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
            inventario.setStockDisponible(inventario.getStockDisponible() + movimiento.getCantidad());

            // Calcular y asignar el valor total si viene valor unitario
            if (movimiento.getValorUnitario() != null) {
                BigDecimal total = movimiento.getValorUnitario()
                        .multiply(BigDecimal.valueOf(movimiento.getCantidad()));
                movimiento.setValorTotal(total);
            }

        } else if (movimiento.getTipoMovimiento() == TipoMovimiento.VENTA) {
            inventario.setStockDisponible(inventario.getStockDisponible() - movimiento.getCantidad());
        }

        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioProductoRepository.save(inventario);

        // Registrar el movimiento en GestionStock a través del servicio (con
        // descripción)
        String descripcionMovimiento = "Movimiento de tipo " + movimiento.getTipoMovimiento().name();
        gestionStockService.registrarMovimiento(
                sku,
                movimiento.getCantidad(),
                inventario.getEstadoStockActual(),
                empleado.getNombre(),
                descripcionMovimiento);

        // Asignar la fecha del movimiento
        // movimiento.setFechaMovimiento(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }

    // Eliminar un movimiento por ID
    public void eliminarMovimiento(Integer id) {
        if (!movimientoRepository.existsById(id)) {
            throw new IllegalArgumentException("El movimiento de inventario no existe");
        }
        movimientoRepository.deleteById(id);
    }

    // Método auxiliar: crear inventario si no existe
    private InventarioProducto crearInventarioInicial(String sku) {
        InventarioProducto nuevo = new InventarioProducto();
        nuevo.setSku(sku);
        nuevo.setStockDisponible(0);
        nuevo.setStockBloqueado(0);
        nuevo.setStockAgotado(0);
        nuevo.setEstadoStockActual(EstadoStock.DISPONIBLE);
        nuevo.setUltimaActualizacion(LocalDateTime.now());
        return inventarioProductoRepository.save(nuevo);
    }
}
