package com.smartmarket.api.auth;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio personalizado que implementa UserDetailsService
 * para que Spring Security pueda cargar un Empleado desde la base de datos
 * usando el email como username.
 */
@Service
public class EmpleadoUserDetailsService implements UserDetailsService {

    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoUserDetailsService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Método que carga un usuario desde la base de datos según el email.
     * @param username Email del empleado (Spring Security lo llama "username")
     * @return UserDetails con la información del empleado
     * @throws UsernameNotFoundException Si no se encuentra el empleado
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Autenticado a: " + username);
        return empleadoRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Empleado no encontrado con email: " + username
                ));
    }
}
