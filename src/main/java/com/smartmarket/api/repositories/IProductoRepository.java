package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoriaNombreContainingIgnoreCase(
        String nombre, String descripcion, String sku, String categoria);
    Optional<Producto> findById(Long id);
}
