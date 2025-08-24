package com.smartmarket.api.auth;

import jakarta.validation.constraints.NotBlank;

public class CambioPasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contraseñaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaContraseña;

    // Constructor vacío
    public CambioPasswordRequest() {}

    // Constructor completo
    public CambioPasswordRequest(String contraseñaActual, String nuevaContraseña) {
        this.contraseñaActual = contraseñaActual;
        this.nuevaContraseña = nuevaContraseña;
    }

    // Getters y Setters
    public String getContraseñaActual() {
        return contraseñaActual;
    }

    public void setContraseñaActual(String contraseñaActual) {
        this.contraseñaActual = contraseñaActual;
    }

    public String getNuevaContraseña() {
        return nuevaContraseña;
    }

    public void setNuevaContraseña(String nuevaContraseña) {
        this.nuevaContraseña = nuevaContraseña;
    }
}