package com.smartmarket.api.auth;

import com.smartmarket.api.models.Empleado;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class EmpleadoUserDetails implements UserDetails {

    private final Empleado empleado;

    public EmpleadoUserDetails(Empleado empleado) {
        this.empleado = empleado;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Aquí podrías mapear roles, por ahora damos un rol fijo
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return empleado.getPassword(); // Asegúrate de que el campo exista
    }

    @Override
    public String getUsername() {
        return empleado.getEmail(); // Autenticación por email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Cambiar si manejas expiración de cuentas
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Cambiar si manejas bloqueo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Cambiar si manejas expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // Cambiar si manejas estado activo/inactivo
    }
}
