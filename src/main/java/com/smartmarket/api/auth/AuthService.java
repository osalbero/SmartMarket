package com.smartmarket.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartmarket.api.models.Cargo;
import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.models.Role;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import com.smartmarket.api.repositories.ICargoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IEmpleadoRepository empleadoRepository;
    private final ICargoRepository cargoRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Lógica para el login de un empleado
    // AuthService.java o donde tengas tu lógica de autenticación
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Autenticar al usuario con email y contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // 2. Buscar al empleado en la base de datos
            Empleado empleado = empleadoRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));

            // 3. Crear el UserDetails personalizado
            UserDetails userDetails = new EmpleadoUserDetails(empleado);

            // 4. Generar el token JWT con claims personalizados
            String jwtToken = jwtService.generateToken(userDetails);

            // 5. Retornar la respuesta con el token y datos adicionales
            return AuthResponse.builder()
                    .token(jwtToken)
                    .nombre(empleado.getNombre())
                    .id(empleado.getId() != null ? empleado.getId().longValue() : null)
                    .primerIngreso(Boolean.TRUE.equals(empleado.getPrimerIngreso()))
                    .build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas", e);
        }
    }

    // Lógica para registrar un nuevo empleado
    public AuthResponse register(RegisterRequest request) {
        // 0. Validación mínima
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseña son obligatorios");
        }

        // 1. Verificar email duplicado
        if (empleadoRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        // 2. Buscar o crear cargo por nombre (request.getNombreCargo())
        Cargo cargo = cargoRepository.findByNombre(request.getNombreCargo())
                .orElseGet(() -> {
                    Cargo nuevo = new Cargo();
                    nuevo.setNombre(request.getNombreCargo());
                    return cargoRepository.save(nuevo);
                });

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = "Abcd1234"; // password por defecto
        }
        // 3. Construir Empleado (no setear nombreCargo)
        Empleado empleado = Empleado.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .cargo(cargo)
                .role(request.getRol() != null ? request.getRol() : Role.USER) // o el que corresponde a la lógica
                .primerIngreso(true)
                .build();

        // 4. Guardar
        System.out.println("Empleado a guardar: " + empleado);
        System.out.println("Password (encriptado): " + empleado.getPassword());
        System.out.println("Rol: " + empleado.getRole());
        System.out.println("Primer ingreso: " + empleado.getPrimerIngreso());

        Empleado guardado = empleadoRepository.save(empleado);

        // 5. Generar token con el ID del empleado
        String jwtToken = jwtService.generateToken(guardado);

        return AuthResponse.builder().token(jwtToken).nombre(empleado.getNombre()).primerIngreso(true).build();
    }

}
