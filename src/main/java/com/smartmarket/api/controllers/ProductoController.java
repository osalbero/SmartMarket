package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.models.InventarioProducto;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import com.smartmarket.api.repositories.IProductoRepository;
import com.smartmarket.api.repositories.InventarioProductoRepository;
import com.smartmarket.api.services.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    private IProductoRepository productoRepository;
    private IEmpleadoRepository empleadoRepository;
    private InventarioProductoRepository inventarioProductoRepository;
    private BigDecimal nuevoPrecio;

    public ProductoController(ProductoService productoService,
            IProductoRepository productoRepository,
            IEmpleadoRepository empleadoRepository,
            InventarioProductoRepository inventarioProductoRepository) {
        this.productoService = productoService;
        this.productoRepository = productoRepository;
        this.empleadoRepository = empleadoRepository;
        this.inventarioProductoRepository = inventarioProductoRepository;
    }

    // Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    // Obtener un producto por SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<?> getProductoBySku(@PathVariable String sku) {
        // 1. Buscar el Producto por SKU
        Optional<Producto> productoOpt = productoRepository.findBySku(sku);
        if (productoOpt.isEmpty()) {
            // Si el producto no se encuentra, devuelve 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto con SKU " + sku + " no encontrado.");
        }
        Producto producto = productoOpt.get();

        // 2. Buscar el InventarioProducto correspondiente por SKU
        Optional<InventarioProducto> inventarioOpt = inventarioProductoRepository.findBySku(sku);
        int stockDisponible = 0; // Valor por defecto si no hay entrada en inventario

        if (inventarioOpt.isPresent()) {
            // Si se encuentra la entrada de inventario, usa el stock disponible
            stockDisponible = inventarioOpt.get().getStockDisponible();
        } else {
            // Opcional: Si no hay entrada de inventario, puedes considerar esto como un
            // error
            // o simplemente un stock de 0. Para este caso, lo dejamos en 0.
            System.out.println("Advertencia: No se encontró entrada de inventario para SKU: " + sku);
        }

        // 3. Construir la respuesta combinada que el frontend espera
        Map<String, Object> response = new HashMap<>();
        response.put("id", producto.getId());
        response.put("sku", producto.getSku());
        response.put("nombre", producto.getNombre());
        response.put("precio", producto.getPrecioVenta()); // Asumiendo que 'precioVenta' es el precio que el frontend
                                                           // necesita
        response.put("stock", stockDisponible); // ¡Aquí está la propiedad 'stock' que el frontend espera!

        return ResponseEntity.ok(response);
    }

    // Buscar productos por nombre, descripción o SKU
    @GetMapping("/buscar")
    public List<Producto> buscarProductos(@RequestParam String query) {
        return productoService.buscarPorNombreDescripcionOSku(query);
    }

    // Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.obtenerPorId(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo producto
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.crearProductoConCategoria(producto);
        return ResponseEntity.ok(nuevoProducto);
    }

    // Crear múltiples productos (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Producto>> crearProductosEnLote(@RequestBody List<Producto> productos) {
        List<Producto> nuevosProductos = productoService.crearProductosEnLote(productos);
        return ResponseEntity.ok(nuevosProductos);
    }

    // Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(actualizado);
    }

    // Actualizar un producto por SKU
    @PutMapping("/sku/{sku}")
    public ResponseEntity<Producto> actualizarPorSku(@PathVariable String sku, @RequestBody Producto producto) {
        Producto actualizado = productoService.actualizarProductoPorSku(sku, producto);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un producto por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sku/{sku}/precio")
    public ResponseEntity<?> actualizarPrecio(@PathVariable String sku, @RequestBody Map<String, Object> datos) {
        try {
            BigDecimal precioNuevo = new BigDecimal(datos.get("precioNuevo").toString());

            Empleado empleado = empleadoRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            return productoService.actualizarPrecioPorSku(sku, precioNuevo, empleado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{sku}/precio")
    public ResponseEntity<Map<String, Object>> actualizarPrecioPorSku(
            @PathVariable String sku,
            @RequestBody Map<String, Object> datos) {

        // Aquí obtienes el nuevo precio enviado en el body
        BigDecimal nuevoPrecio = new BigDecimal(datos.get("precio").toString());

        // Convertir el empleado recibido en el body
        Map<String, Object> empleadoData = (Map<String, Object>) datos.get("empleado");
        Empleado empleado = new Empleado();
        // Aquí obtienes el empleado (puede ser desde BD, sesión o crear uno temporal)
        empleado.setId(Integer.valueOf(empleadoData.get("id").toString()));
        empleado.setNombre(empleadoData.get("nombre").toString());
        // O si solo quieres dejarlo temporal:
        // empleado.setNombre("Empleado temporal");

        // Llamas al servicio para actualizar
        productoService.actualizarPrecioPorSku(sku, nuevoPrecio, empleado);

        // Preparas la respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Precio actualizado correctamente");
        respuesta.put("precio", nuevoPrecio);

        return ResponseEntity.ok(respuesta);
    }

}