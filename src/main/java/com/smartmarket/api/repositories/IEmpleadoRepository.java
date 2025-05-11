package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, Integer> {
    boolean existsByEmail(String email);
}