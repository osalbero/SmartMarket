package com.smartmarket.api.repositories;

import com.smartmarket.api.models.GestionStock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IGestionStockRepository extends JpaRepository<GestionStock, Integer> {

    List<GestionStock> findBySku(String sku);
}
