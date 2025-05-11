package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Proveedor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProveedorRepository extends JpaRepository<Proveedor, Integer> {
    boolean existsByEmail(String email);
    Optional<Proveedor> findByEmail(String email);
}