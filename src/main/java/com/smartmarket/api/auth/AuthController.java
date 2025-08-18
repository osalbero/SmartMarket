package com.smartmarket.api.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@CrossOrigin(origins = "*")   
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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
                String username = jwtService.getUsernameFromToken(token);
                System.out.println("Usuario del token: " + username);
                // Aquí podrías cargar el usuario desde tu base de datos si lo necesitas
                return ResponseEntity.ok("Token válido ✅ para usuario: " + username);
            } catch (Exception e) {
                System.out.println("Error al validar token: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido ❌");
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido ❌");
    }


}
