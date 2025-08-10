package com.smartmarket.api.repositories;

import com.smartmarket.api.models.MovimientoDeInventario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMovimientoDeInventarioRepository extends JpaRepository<MovimientoDeInventario, Integer> {
    List<MovimientoDeInventario> findBySku(String sku);
    List<MovimientoDeInventario> findByCantidadGreaterThan(int cantidad);
    List<MovimientoDeInventario> findBySkuContainingIgnoreCaseOrProducto_NombreContainingIgnoreCase(String sku, String nombreProducto);
}