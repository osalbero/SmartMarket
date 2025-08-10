package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "gestion_stock")
@Getter
@Setter
public class GestionStock {

    public GestionStock() {
        this.fechaMovimiento = LocalDateTime.now();        
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoStock estadoStock;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "usuario_responsable", nullable = false, length = 100)
    private String usuarioResponsable;

    @Column(name = "descripcion_movimiento", length = 255)
    private String descripcionMovimiento;
}
