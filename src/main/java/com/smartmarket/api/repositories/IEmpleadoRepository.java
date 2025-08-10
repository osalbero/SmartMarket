package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Empleado> findByNombreContainingIgnoreCaseOrCargoNombreContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String cargo, String telefono, String email);
}