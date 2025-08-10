package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_producto")
@Getter
@Setter
public class InventarioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "stock_disponible", nullable = false)
    private int stockDisponible;

    @Column(name = "stock_bloqueado", nullable = false)
    private int stockBloqueado;

    @Column(name = "stock_agotado", nullable = false)
    private int stockAgotado;

    @Enumerated(EnumType.STRING)
    private EstadoStock estadoStockActual;

    private LocalDateTime ultimaActualizacion;
}
