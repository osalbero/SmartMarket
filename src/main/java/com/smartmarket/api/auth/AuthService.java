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
        try {
            // 1. Autentica al usuario usando el email y la contraseña
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // 2. Si la autenticación es exitosa, busca al empleado para obtener su ID
            Empleado empleado = empleadoRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));

            // 3. Genera el token JWT que incluya el ID del empleado
            // Asume que tu método generateToken puede tomar un segundo parámetro para el ID
            String jwtToken = jwtService.generateToken(empleado);

            // 4. Crea y devuelve el objeto AuthResponse que contiene el token y el ID del empleado
            return AuthResponse.builder()
                    .token(jwtToken)
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

        // 3. Construir Empleado (no setear nombreCargo)
        Empleado empleado = Empleado.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .cargo(cargo)
                .role(Role.USER) // o el que corresponde a la lógica
                .build();

        // 4. Guardar
        Empleado guardado = empleadoRepository.save(empleado);

        // 5. Generar token con el ID del empleado
        String jwtToken = jwtService.generateToken(guardado);

        return AuthResponse.builder().token(jwtToken).build();
    }

}
