package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Categoria;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Categoria> findByNombre(String nombre);
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
}