package com.smartmarket.api.models;

import java.time.LocalDateTime;

public class InventarioProductoDTO {
    private String sku;
    private String nombre;
    private int stockDisponible;
    private int stockBloqueado;
    private int stockAgotado;
    private EstadoStock estadoStockActual;
    private LocalDateTime ultimaActualizacion;

    public InventarioProductoDTO() {
    }

    public InventarioProductoDTO(String sku, String nombre, int stockDisponible, int stockBloqueado, int stockAgotado, EstadoStock estadoStockActual, LocalDateTime ultimaActualizacion) {
        this.sku = sku;
        this.nombre = nombre;
        this.stockDisponible = stockDisponible;
        this.stockBloqueado = stockBloqueado;
        this.stockAgotado = stockAgotado;
        this.estadoStockActual = estadoStockActual;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(int stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public int getStockBloqueado() {
        return stockBloqueado;
    }

    public void setStockBloqueado(int stockBloqueado) {
        this.stockBloqueado = stockBloqueado;
    }

    public int getStockAgotado() {
        return stockAgotado;
    }

    public void setStockAgotado(int stockAgotado) {
        this.stockAgotado = stockAgotado;
    }

    public EstadoStock getEstadoStockActual() {
        return estadoStockActual;
    }

    public void setEstadoStockActual(EstadoStock estadoStockActual) {
        this.estadoStockActual = estadoStockActual;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

}
