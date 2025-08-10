package com.smartmarket.api.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.smartmarket.api.models.Cliente;
import com.smartmarket.api.models.DetalleVenta;
import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.models.InventarioProducto;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.models.Venta;
import com.smartmarket.api.repositories.IClienteRepository;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import com.smartmarket.api.repositories.IProductoRepository;
import com.smartmarket.api.repositories.IVentaRepository;
import com.smartmarket.api.repositories.InventarioProductoRepository;

@Service

public class VentaService {

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private InventarioProductoRepository inventarioProductoRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    public ResponseEntity<?> registrarVenta(Long idCliente, Long idEmpleado,
            List<Map<String, Object>> productosVendidos) {
        try {
            // Buscar cliente y empleado
            Cliente cliente = clienteRepository.findById(idCliente.intValue())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            Empleado empleado = empleadoRepository.findById(idEmpleado.intValue())
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            // Crear la venta
            Venta venta = new Venta();
            venta.setFecha(LocalDateTime.now());
            venta.setCliente(cliente);
            venta.setEmpleado(empleado);
            int aleatorio = (int) (Math.random() * 900) + 100; // entre 100 y 999
            String numeroFactura = "FCT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + aleatorio;
            venta.setNumeroFactura(numeroFactura);
            // Generar número de factura

            BigDecimal totalVenta = BigDecimal.ZERO;
            List<DetalleVenta> detalles = new ArrayList<>();

            // Recorrer productos vendidos
            for (Map<String, Object> item : productosVendidos) {
                String sku = (String) item.get("sku");
                int cantidad = (int) item.get("cantidad");

                // Buscar producto e inventario
                Producto producto = productoRepository.findBySku(sku)
                        .orElseThrow(() -> new IllegalArgumentException("Producto con SKU " + sku + " no encontrado"));

                InventarioProducto inventario = inventarioProductoRepository.findBySku(sku)
                        .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado para SKU " + sku));

                // Verificar stock disponible
                if (inventario.getStockDisponible() < cantidad) {
                    return ResponseEntity.badRequest()
                            .body("Stock insuficiente para el producto: " + producto.getNombre());
                }

                // Calcular subtotal
                BigDecimal precioUnitario = producto.getPrecioVenta(); // Se toma de la BD automáticamente
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

                // Actualizar inventario
                inventario.setStockDisponible(inventario.getStockDisponible() - cantidad);
                inventario.setUltimaActualizacion(LocalDateTime.now());
                inventarioProductoRepository.save(inventario);

                // Crear detalle de venta
                DetalleVenta detalle = new DetalleVenta();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setSubtotal(subtotal);
                detalle.setVenta(venta);

                detalles.add(detalle);
                totalVenta = totalVenta.add(subtotal);
            }

            // Asociar detalles y total a la venta
            venta.setTotal(totalVenta);
            venta.setDetalles(detalles);

            // Guardar la venta
            ventaRepository.save(venta);

            return ResponseEntity.ok("Venta registrada correctamente con ID: " + venta.getId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la venta: " + e.getMessage());
        }
    }

    public Venta guardarVenta(Venta venta) {
        venta.setFecha(LocalDateTime.now());
        return ventaRepository.save(venta);
    }

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

}
