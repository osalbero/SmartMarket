package com.smartmarket.api.models;

import java.math.BigDecimal;

// Importación de anotaciones de JPA y Lombok
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase que representa la entidad Producto para ser gestionada
 * por el framework JPA en la base de datos.
 */
@Entity // Indica que esta clase es una entidad JPA
@Getter // Lombok: genera automáticamente los getters
@Setter // Lombok: genera automáticamente los setters
@Table(name = "producto") // Define el nombre de la tabla en la BD
public class Producto {

    @Id // Identificador único del producto (clave primaria)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrementable
    @Column(name = "id_producto") // Nombre de la columna en la base de datos
    private Integer id;

    @Column(name = "sku", unique = true, nullable = false, length = 100) 
    // SKU: clave única del producto, no puede ser nula
    private String sku;

    @Column(name = "nombre_producto", nullable = false, length = 100)
    // Nombre del producto
    private String nombre;

    @Column(name = "descripcion_producto", nullable = false)
    // Descripción detallada del producto
    private String descripcion;

    @Column(name = "nombre_categoria")
    // Nombre de la categoría como texto (opcional, posiblemente redundante)
    private String nombreCategoria;

    @Column(name = "codigo_de_barras", length = 100)
    // Código de barras del producto (opcional)
    private String codigoDeBarras;

    @ManyToOne // Relación muchos a uno con la entidad Categoría
    @JoinColumn(name = "id_categoria", nullable = false)
    // Llave foránea que conecta el producto con una categoría existente
    private Categoria categoria;

    // Precio de venta del producto
    @Column(name = "precio_venta", nullable = false)
    private BigDecimal precioVenta;
}
// Fin de la clase Producto