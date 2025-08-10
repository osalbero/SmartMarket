package com.smartmarket.api.services;

import com.smartmarket.api.models.*;
import com.smartmarket.api.repositories.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final IProductoRepository productoRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IEmpleadoRepository empleadoRepository;
    private final IHistoricoPrecioRepository historicoPrecioRepository;

    public ProductoService(IProductoRepository productoRepository, IClienteRepository IClienteRepository,
            ICategoriaRepository categoriaRepository, IEmpleadoRepository empleadoRepository,
            IHistoricoPrecioRepository historicoPrecioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.empleadoRepository = empleadoRepository;
        this.historicoPrecioRepository = historicoPrecioRepository;
    }

    // Obtener todos los productos
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Buscar productos por nombre, descripción o SKU
    public List<Producto> buscarPorNombreDescripcionOSku(String query) {
        return productoRepository
                .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoriaNombreContainingIgnoreCase(
                        query, query, query, query);
    }

    // Obtener un producto por ID
    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }

    // Crear un nuevo producto
    public Producto crearProducto(Producto producto) {
        if (productoRepository.existsBySku(producto.getSku())) {
            throw new RuntimeException("El SKU '" + producto.getSku() + "' ya existe.");
        }
        return productoRepository.save(producto);
    }

    // Crear un nuevo producto con categoría
    public Producto crearProductoConCategoria(Producto producto) {
        // 1. Buscar categoría por ID
        Optional<Categoria> categoria = categoriaRepository.findById(producto.getCategoria().getId());
        if (categoria.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La categoría '" + producto.getCategoria().getNombre() + "' no existe.");
        }

        // 2. Generar SKU automáticamente si no se proporciona
        if (producto.getSku() == null || producto.getSku().isBlank()) {
            producto.setSku(generarSkuUnico());
        }

        // 3. Verificar si el SKU ya existe
        if (productoRepository.findBySku(producto.getSku()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con el SKU: " + producto.getSku());
        }

        // 4. Asignar la categoría y su nombre
        producto.setCategoria(categoria.get());
        producto.setNombreCategoria(categoria.get().getNombre());

        // 5. Guardar el producto
        return productoRepository.save(producto);
    }

    // Crear múltiples productos por categoría (Bulk Insert)
    public List<Producto> crearProductosEnLote(List<Producto> productos) {
        for (Producto producto : productos) {
            Optional<Categoria> categoria = categoriaRepository.findByNombre(producto.getCategoria().getNombre());
            if (categoria.isPresent()) {
                producto.setCategoria(categoria.get()); // Asignar la categoría existente al producto
                producto.setNombreCategoria(categoria.get().getNombre()); // Asignar el nombre de la categoría al
                                                                          // producto
            } else {
                throw new RuntimeException("La categoría'" + producto.getCategoria().getNombre() + "' no existe.");
            }
        }
        return productoRepository.saveAll(productos);
    }

    // Obtener un producto por SKU
    public Optional<Producto> obtenerPorSku(String sku) {
        return productoRepository.findBySku(sku);
    }

    // Actualizar un producto por SKU
    // Este método actualiza el producto por SKU y registra el cambio de precio en
    // el histórico
    public Producto actualizarProductoPorSku(String sku, Producto productoActualizado) {
        return productoRepository.findBySku(sku)
                .map(producto -> {
                    Integer idCategoria = productoActualizado.getCategoria().getId();

                    // Buscar o crear la categoría
                    Categoria categoria = categoriaRepository.findById(idCategoria)
                            .orElseGet(() -> {
                                Categoria nuevaCategoria = new Categoria();
                                nuevaCategoria.setId(idCategoria);
                                nuevaCategoria.setNombre(productoActualizado.getCategoria().getNombre());
                                return categoriaRepository.save(nuevaCategoria);
                            });

                    producto.setCategoria(categoria);

                    // Validación y actualización de precioVenta
                    BigDecimal precioAnterior = producto.getPrecioVenta();
                    BigDecimal nuevoPrecio = productoActualizado.getPrecioVenta();
                    if (nuevoPrecio != null && precioAnterior != null && nuevoPrecio.compareTo(precioAnterior) != 0) {
                        // Registrar en histórico
                        HistoricoPrecio historico = new HistoricoPrecio();
                        historico.setProducto(producto);
                        historico.setPrecioAnterior(precioAnterior);
                        historico.setPrecioNuevo(nuevoPrecio);
                        historico.setFechaActualizacion(LocalDateTime.now());

                        historicoPrecioRepository.save(historico);

                        producto.setPrecioVenta(nuevoPrecio);
                    }

                    // Actualizar otros datos
                    producto.setNombre(productoActualizado.getNombre());
                    producto.setDescripcion(productoActualizado.getDescripcion());
                    producto.setCodigoDeBarras(productoActualizado.getCodigoDeBarras());

                    return productoRepository.save(producto);
                })
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con SKU: " + sku));
    }

    // Usar este método solo si el flujo trabaja con ID de producto
    public Producto actualizarProducto(Integer id, Producto productoActualizado) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Buscar la categoría por nombre
        Optional<Categoria> categoria = categoriaRepository
                .findByNombre(productoActualizado.getCategoria().getNombre());

        if (categoria.isPresent()) {
            productoExistente.setCategoria(categoria.get());
            productoExistente.setNombreCategoria(categoria.get().getNombre()); // ✅ Actualizar nombre de la categoría
        }

        productoExistente.setCategoria(categoria.get());
        productoExistente.setNombreCategoria(categoria.get().getNombre()); // ✅ Actualizar nombre de la categoría
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setCodigoDeBarras(productoActualizado.getCodigoDeBarras());

        return productoRepository.save(productoExistente);
    }

    // Eliminar un producto por ID
    public void eliminarProducto(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("El producto no existe");
        }
        productoRepository.deleteById(id);
    }

    // Método privado para generar un SKU único
    private String generarSkuUnico() {
        String sku;
        do {
            sku = generarSku();
        } while (productoRepository.existsBySku(sku));
        return sku;
    }

    // Generador base: PRD + AÑO + MES + 3 dígitos aleatorios
    private String generarSku() {
        LocalDate ahora = LocalDate.now();
        int AÑO = ahora.getYear();
        int MES = ahora.getMonthValue();

        int aleatorio = (int) (Math.random() * 900) + 100; // Genera un número aleatorio de 3 dígitos
        return "PRD" + AÑO + String.format("%02d", MES) + aleatorio;
    }

    public ResponseEntity<?> actualizarPrecioPorSku(String sku, BigDecimal precioNuevo, Empleado empleado) {
        Optional<Producto> optionalProducto = productoRepository.findBySku(sku);

        if (optionalProducto.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }

        Producto producto = optionalProducto.get();
        BigDecimal precioAnterior = producto.getPrecioVenta();

        if (precioAnterior.compareTo(precioNuevo) == 0) {
            return ResponseEntity.badRequest().body("El nuevo precio es igual al actual");
        }

        // Crear y guardar en histórico
        HistoricoPrecio historico = new HistoricoPrecio();
        historico.setProducto(producto);
        historico.setPrecioAnterior(precioAnterior);
        historico.setPrecioNuevo(precioNuevo);
        historico.setFechaActualizacion(LocalDateTime.now());
        historico.setEmpleado(empleado);

        historicoPrecioRepository.save(historico);

        // Actualizar el precio del producto
        producto.setPrecioVenta(precioNuevo);
        productoRepository.save(producto);

        return ResponseEntity.ok("Precio actualizado correctamente y registrado en histórico");
    }

}