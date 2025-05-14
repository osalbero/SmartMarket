package com.smartmarket.api.services;

import com.smartmarket.api.models.Categoria;
import com.smartmarket.api.models.Producto;
import com.smartmarket.api.repositories.IClienteRepository;
import com.smartmarket.api.repositories.IProductoRepository;
import com.smartmarket.api.repositories.ICategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final IProductoRepository productoRepository;
    private final ICategoriaRepository categoriaRepository;

    public ProductoService(IProductoRepository productoRepository, IClienteRepository IClienteRepository,
            ICategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Obtener todos los productos
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
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
        Optional<Categoria> categoria = categoriaRepository.findByNombre(producto.getCategoria().getNombre());
        if (categoria.isEmpty()) {
            throw new RuntimeException("La categoría'" + producto.getCategoria().getNombre() + "' no existe.");
        }

        // Verificar si el SKU ya existe
        if (productoRepository.findBySku(producto.getSku()).isPresent()) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + producto.getSku());
        }

        producto.setCategoria(categoria.get()); // Asignar la categoría existente al producto
        producto.setNombreCategoria(categoria.get().getNombre()); // Asignar el nombre de la categoría al producto
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

    // Usar este metodo para flujos donde el SKU ens la clave principal
    public Producto actualizarProductoPorSku(String sku, Producto productoActualizado) {
    return productoRepository.findBySku(sku)
            .map(producto -> {
                String nombreCategoria = productoActualizado.getCategoria().getNombre();

                // Buscar si la categoría ya existe
                Categoria categoria = categoriaRepository.findByNombre(nombreCategoria)
                        .orElseGet(() -> {
                            // Si no existe, crear una nueva categoría
                            Categoria nuevaCategoria = new Categoria();
                            nuevaCategoria.setNombre(nombreCategoria);
                            return categoriaRepository.save(nuevaCategoria);
                        });

                // Asignar la categoría al producto
                producto.setCategoria(categoria);
                producto.setNombreCategoria(categoria.getNombre()); // Si usas nombreCategoria como campo

                // Actualizar otros datos del producto
                producto.setNombre(productoActualizado.getNombre());
                producto.setDescripcion(productoActualizado.getDescripcion());
                producto.setCodigoDeBarras(productoActualizado.getCodigoDeBarras());

                return productoRepository.save(producto);
            })
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
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
}