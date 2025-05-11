package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer id;

    @Column(name = "nombre_proveedor", nullable = false, length = 100)
    private String nombre;

    @Column(name = "direccion_proveedor", nullable = false, length = 100)
    private String direccion;

    @Column(name = "telefono_proveedor", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email_proveedor", nullable = false, length = 100, unique = true)
    private String email;
}