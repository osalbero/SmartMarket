package com.smartmarket.api.auth;

import com.smartmarket.api.models.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nombre;
    private String telefono;
    private String email;
    private String nombreCargo;
    private String password;
    private Role rol; // Puede ser ADMIN, USER, CAJERO, etc.

}
