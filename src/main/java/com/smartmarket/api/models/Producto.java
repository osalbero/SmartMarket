package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;

    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion_producto", nullable = false)
    private String descripcion;

    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @Column(name = "codigo_de_barras", length = 100)
    private String codigoDeBarras;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}