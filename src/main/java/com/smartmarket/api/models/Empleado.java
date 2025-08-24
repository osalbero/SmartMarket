package com.smartmarket.api.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empleado")
public class Empleado implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer id;

    @Column(name = "nombre_empleado", nullable = false, length = 100)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_cargo", nullable = false)
    @JsonBackReference
    private Cargo cargo;

    @Column(name = "telefono_empleado", nullable = false, length = 20)
    private String telefono;

    @Column(name = "email_empleado", nullable = false, length = 100, unique = true)
    private String email;

    @JsonProperty("nombreCargo")
    public String getNombreCargo() {
        return cargo != null ? cargo.getNombre() : "Sin cargo";
    }

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            System.out.println("⚠️ El rol del empleado es null: " + email);
            return List.of(); // o puedes retornar un rol por defecto
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Column(name = "primer_ingreso", nullable = false)
    private Boolean primerIngreso;


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
