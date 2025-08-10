package com.smartmarket.api.controllers;

import com.smartmarket.api.models.Categoria;
import com.smartmarket.api.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // permite cualquier origen
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Obtener todas las categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> obtenerTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    // Obtener una categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    // Buscar categorías por nombre
    @GetMapping("/buscar")
    public List<Categoria> buscarCategorias(@RequestParam String query) {
        return categoriaService.buscarPorNombre(query);
    }

    // Crear una nueva categoría
    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.crearCategoria(categoria.getNombre());
        return ResponseEntity.ok(nuevaCategoria);
    }

    // Crear múltiples categorías (Bulk Insert)
    @PostMapping("/lote")
    public ResponseEntity<List<Categoria>> crearCategorias(@RequestBody List<Categoria> categorias) {
        List<Categoria> nuevasCategorias = categoriaService.crearCategorias(categorias);
        return ResponseEntity.ok(nuevasCategorias);
    }

    // Eliminar una categoría por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // Actualizar una categoría por ID
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        Categoria actualizada = categoriaService.actualizarCategoria(id, categoria);
        return ResponseEntity.ok(actualizada);
    }
}
