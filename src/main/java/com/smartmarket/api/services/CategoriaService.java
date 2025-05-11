package com.smartmarket.api.services;

import com.smartmarket.api.models.Categoria;
import com.smartmarket.api.repositories.ICategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final ICategoriaRepository categoriaRepository;

    public CategoriaService(ICategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Crear múltiples categorías (Bulk Insert)
    public List<Categoria> crearCategorias(List<Categoria> categorias) {
        for (Categoria categoria : categorias) {
            if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
                throw new IllegalArgumentException("La categoría " + categoria.getNombre() + " ya existe");
            }
        }
        return categoriaRepository.saveAll(categorias);
    }
    
    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    // Buscar una categoría por ID
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    // Crear una nueva categoría si no existe
    public Categoria crearCategoria(String nombre) {
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("La categoría ya existe");
        }
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(nombre);
        return categoriaRepository.save(nuevaCategoria);
    }

    // Eliminar una categoría por ID
    public void eliminarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("La categoría no existe");
        }
        categoriaRepository.deleteById(id);
    }

    // Actualizar una categoría por ID
    public Categoria actualizarCategoria(Integer id, Categoria categoria) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("La categoría no existe");
        }
        Categoria categoriaExistente = categoriaRepository.findById(id).orElseThrow();
        categoriaExistente.setNombre(categoria.getNombre());
        return categoriaRepository.save(categoriaExistente);
    }
}