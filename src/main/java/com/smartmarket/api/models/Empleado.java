package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "cargo", nullable = false, length = 50)
    private String cargo;

    @Column(name = "telefono_empleado", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email_empleado", nullable = false, length = 100, unique = true)
    private String email;
}