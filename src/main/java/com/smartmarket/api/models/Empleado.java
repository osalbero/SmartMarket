package com.smartmarket.api.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Table(name = "empleado")
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer id;

    @Column(name = "nombre_empleado", nullable = false, length = 100)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_cargo", nullable = false)
    @JsonBackReference
    private Cargo cargo;

    @Column(name = "telefono_empleado", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email_empleado", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "nombre_cargo")
    // Nombre del cargo del empleado (opcional, posiblemente redundante)
    private String nombreCargo;

    @Column(name = "password", nullable = false) // Nuevo campo
    private String password;

    @Column(name = "roles") // Nuevo campo
    private String roles;

    
}