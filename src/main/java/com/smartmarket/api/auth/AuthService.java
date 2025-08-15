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
    public AuthResponse login(LoginRequest request) {
        // 1. Autentica al usuario usando el email y la contraseña
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // 2. Si la autenticación es exitosa, Busca al usuario en la base de datos
        UserDetails user = empleadoRepository.findByEmail(request.getEmail()).orElseThrow();

        // 3. Genera el token JWT para el usuario
        String jwtToken = jwtService.generateToken(user);

        // 4. Crea y devuelve el objeto AuthResponse que contiene el token
        return AuthResponse.builder().token(jwtToken).build();
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

        // 3. Construir Empleado (no setear nombreCargo)
        Empleado empleado = Empleado.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cargo(cargo)
                .role(Role.USER) // o el que correspond a la lógica
                .build();

        // 4. Guardar
        Empleado guardado = empleadoRepository.save(empleado);

        // 5. Generar token (si usas JWT)
        String jwtToken = jwtService.generateToken(guardado);

        return AuthResponse.builder().token(jwtToken).build();
    }

}
