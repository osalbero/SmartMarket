package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, Integer> {

    // Buscar por email exacto (clave para autenticación)
    Optional<Empleado> findByEmail(String email);

    // Validar si ya existe un email
    boolean existsByEmail(String email);

    // Búsqueda general (nombre, cargo, teléfono o email)
    @EntityGraph(attributePaths = { "cargo" })
    List<Empleado> findByNombreContainingIgnoreCaseOrCargoNombreContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nombre,
            String cargo,
            String telefono,
            String email);

}
