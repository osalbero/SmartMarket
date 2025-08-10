package com.smartmarket.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmarket.api.models.Venta;

@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

    
}
