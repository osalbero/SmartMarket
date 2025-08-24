package com.smartmarket.api.auth;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// Marca la clase como un componente de Spring, para que pueda ser inyectada
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final EmpleadoUserDetailsService userDetailsService;

    // Método principal del filtro que se ejecuta en cada petición
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        if (token != null) {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("❌ Token inválido para usuario: " + username);
                }
            }

            System.out.println("🔍 Ruta solicitada: " + path);
            System.out.println("🔐 Token recibido: " + token);
            System.out.println("👤 Usuario extraído: " + username);
            System.out
                    .println("✅ Autenticación en contexto: " + SecurityContextHolder.getContext().getAuthentication());
        }

        filterChain.doFilter(request, response);
    }

private boolean isPublicPath(String path) {
    return (
        path.startsWith("/api/auth/") &&
        !path.equals("/api/auth/cambiar-password") // ← excepción
    ) || path.startsWith("/css/") ||
         path.startsWith("/js/") ||
         path.startsWith("/images/") ||
         path.equals("/login.html") ||
         path.equals("/register.html") ||
         path.equals("/index.html");
}

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
