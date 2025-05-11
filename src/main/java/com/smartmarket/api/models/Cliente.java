package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombre;

    @Column(name = "telefono_cliente", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email_cliente", nullable = false, length = 100, unique = true)
    private String email;
}