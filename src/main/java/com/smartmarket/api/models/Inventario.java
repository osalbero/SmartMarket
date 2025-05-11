package com.smartmarket.api.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Inventario {
    private List<Producto> productos;
    private List<MovimientoDeInventario> movimientos;

    public Inventario(List<Producto> productos, List<MovimientoDeInventario> movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }
}