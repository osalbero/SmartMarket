package com.smartmarket.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*@Configuration
public class CorsConfig {
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://192.168.1.4:8080") // o "*" para todos
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}*/

/*@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Habilita CORS para todas las rutas de la API
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080", "http://192.168.1.4:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // ¡CORREGIDO! No usar el comodín "*" con allowCredentials(true)
                // Se listan explícitamente los encabezados comunes
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
                .allowCredentials(true);
    }
}*/