package com.smartmarket.api.repositories;

import com.smartmarket.api.models.HistoricoPrecio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IHistoricoPrecioRepository extends JpaRepository<HistoricoPrecio, Integer> {
    List<HistoricoPrecio> findByProductoSku(String sku);
}
