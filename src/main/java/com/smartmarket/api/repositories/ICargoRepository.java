package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Cargo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICargoRepository extends JpaRepository<Cargo, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Cargo> findByNombre(String nombre);
    Optional<Cargo> findByNombreIgnoreCase(String nombre);
    List<Cargo> findByNombreContainingIgnoreCase(String nombre);
}