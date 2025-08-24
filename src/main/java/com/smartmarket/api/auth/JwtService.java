package com.smartmarket.api.auth;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Marca la clase como un servicio de Spring
@Service
public class JwtService {

    // Se inyecta la clave secreta desde el archivo application.properties
    @Value("${jwt.secret.key}")
    private String secretKey;

    // Se inyecta el tiempo de expiración del token desde application.properties
    @Value("${jwt.time.expiration}")
    private long expirationTime;

    // Extrae el nombre de usuario (subject) del token
    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    // Extrae una claim específica del token usando una función
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrae todas las claims del token
    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Obtiene la clave de firma decodificada
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Valida si el token es válido para un UserDetails específico
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Verifica si el token ha expirado
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    // Obtiene la fecha de expiración del token
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    // Genera un token para un UserDetails con claims extras
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Sobrecarga del método para generar un token sin claims extras
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // 👇 Aquí extraes el rol del UserDetails y lo agregas como claim
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (!authorities.isEmpty()) {
            // Tomamos el primer rol (puedes adaptarlo si hay múltiples)
            String role = authorities.iterator().next().getAuthority();
            claims.put("role", role); // Ejemplo: "ROLE_ADMIN"
        }

        // Nombre del empleado
        if (userDetails instanceof EmpleadoUserDetails empleadoDetails) {
            claims.put("nombre", empleadoDetails.getEmpleado().getNombre());
            claims.put("id", empleadoDetails.getEmpleado().getId());
        }

        return generateToken(userDetails, claims);
    }

}