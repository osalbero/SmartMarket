package com.smartmarket.api.repositories;

import com.smartmarket.api.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByEmail(String email);
}