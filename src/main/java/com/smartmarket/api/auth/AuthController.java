package com.smartmarket.api.auth;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.smartmarket.api.models.Empleado;
import com.smartmarket.api.repositories.IEmpleadoRepository;
import com.smartmarket.api.auth.CambioPasswordRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final IEmpleadoRepository empleadoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inject the secret key from application properties
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // AuthService lanzará ResponseStatusException en caso de conflicto / error
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("Token recibido: " + token);

            try {
                String username = jwtService.extractUsername(token);
                System.out.println("Usuario del token: " + username);

                // Cargar el empleado desde la base de datos
                Empleado empleado = empleadoRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

                // Construir respuesta con datos clave
                Map<String, Object> datos = new HashMap<>();
                datos.put("email", empleado.getEmail());
                datos.put("nombre", empleado.getNombre());
                datos.put("role", empleado.getRole().name());
                datos.put("primerIngreso", Boolean.TRUE.equals(empleado.getPrimerIngreso()));

                return ResponseEntity.ok(datos);

            } catch (Exception e) {
                System.out.println("Error al validar token: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido ❌");
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido ❌");
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@Valid @RequestBody CambioPasswordRequest request) {
        try {
            // ✅ Validación de campos obligatorios
            if (request.getNuevaContraseña() == null || request.getNuevaContraseña().isBlank()) {
                return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía ❌");
            }

            // Extraer el email desde el token JWT ya validado
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            Empleado empleado = empleadoRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            // Validar contraseña actual
            if (!passwordEncoder.matches(request.getContraseñaActual(), empleado.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("La contraseña actual no es válida ❌");
            }

            // Actualizar contraseña y primerIngreso
            empleado.setPassword(passwordEncoder.encode(request.getNuevaContraseña()));
            empleado.setPrimerIngreso(false);
            empleadoRepository.save(empleado);

            return ResponseEntity.ok("Contraseña actualizada correctamente ✅");

        } catch (Exception e) {
            System.out.println("❌ Error al cambiar la contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

}
