package com.smartmarket.api.services;

import com.smartmarket.api.models.Categoria;
import com.smartmarket.api.repositories.ICategoriaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final ICategoriaRepository categoriaRepository;

    public CategoriaService(ICategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Crear múltiples categorías
    public List<Categoria> crearCategorias(List<Categoria> categorias) {
        for (Categoria categoria : categorias) {
            if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría " + categoria.getNombre() + " ya existe");
            }
        }
        return categoriaRepository.saveAll(categorias);
    }

    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    // Buscar por nombre
    public List<Categoria> buscarPorNombre(String query) {
        return categoriaRepository.findByNombreContainingIgnoreCase(query);
    }

    // Buscar por ID
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    // Crear nueva categoría si no existe
    public Categoria crearCategoria(String nombre) {
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría '" + nombre + "' ya existe");
        }
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(nombre);
        return categoriaRepository.save(nuevaCategoria);
    }

    // Eliminar categoría
    public void eliminarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe");
        }

        try {
            categoriaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar la categoría porque tiene productos asociados."
            );
        }
    }

    // Actualizar categoría con validación de duplicado
    public Categoria actualizarCategoria(Integer id, Categoria categoria) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La categoría no existe"));

        // Verificar que no se esté duplicando el nombre (con otro ID)
        Optional<Categoria> otraConMismoNombre = categoriaRepository.findByNombreIgnoreCase(categoria.getNombre());
        if (otraConMismoNombre.isPresent() && !otraConMismoNombre.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otra categoría con el nombre '" + categoria.getNombre() + "'");
        }

        existente.setNombre(categoria.getNombre());
        return categoriaRepository.save(existente);
    }
}
