package com.smartmarket.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmarket.api.models.DetalleVenta;

public interface IDetalleVentaRepository {

    public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {}
}
