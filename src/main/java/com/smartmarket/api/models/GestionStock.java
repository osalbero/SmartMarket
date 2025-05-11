package com.smartmarket.api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "gestion_stock")
public class GestionStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String sku; // Identificador del producto
    private int cantidad; // Cantidad afectada

    @Enumerated(EnumType.STRING)
    private EstadoStock estadoStock; // DISPONIBLE, BLOQUEADO, AGOTADO

    private String usuarioResponsable; // Persona que realizó la acción
    private LocalDateTime fechaMovimiento; // Fecha y hora de la operación
}