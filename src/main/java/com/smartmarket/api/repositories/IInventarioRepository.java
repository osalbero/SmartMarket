package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Producto;
import com.smartmarket.api.models.MovimientoDeInventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


public interface IInventarioRepository extends JpaRepository<Producto, Integer> {

    // Obtener todos los productos con sus cantidades en inventario
    @Query("SELECT p FROM Producto p")
    List<Producto> obtenerInventarioGeneral();

    // Obtener movimientos de inventario por SKU
    @Query("SELECT m FROM MovimientoDeInventario m WHERE m.sku = :sku")
    List<MovimientoDeInventario> obtenerMovimientosPorSku(String sku);
}