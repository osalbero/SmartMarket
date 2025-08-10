package com.smartmarket.api.repositories;

import com.smartmarket.api.models.InventarioProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventarioProductoRepository extends JpaRepository<InventarioProducto, Long> {
    Optional<InventarioProducto> findBySku(String sku);
}
