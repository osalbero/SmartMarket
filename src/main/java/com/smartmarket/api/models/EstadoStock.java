package com.smartmarket.api.models;

public enum EstadoStock {
    DISPONIBLE("Disponible"),
    BLOQUEADO("Bloqueado"),
    AGOTADO("Agotado");

    private final String etiqueta;

    EstadoStock(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
