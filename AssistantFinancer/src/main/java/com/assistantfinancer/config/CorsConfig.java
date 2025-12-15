package com.assistantfinancer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser les requêtes depuis les frontends (Angular, Flutter web, etc.)
        // En dev on accepte toutes les origines pour éviter les 403 CORS
        config.addAllowedOriginPattern("*");
        
        // Autoriser tous les headers
        config.addAllowedHeader("*");
        
        // Autoriser toutes les méthodes HTTP (GET, POST, PUT, DELETE, OPTIONS, etc.)
        config.addAllowedMethod("*");
        
        // Autoriser l'envoi de credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // Exposer l'en-tête Authorization pour que le client puisse le lire
        config.addExposedHeader("Authorization");
        
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}

