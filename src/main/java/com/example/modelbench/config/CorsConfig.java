package com.example.modelbench.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Autorise le frontend Angular, servi sur une autre origine, a appeler l'API.
 */
@Configuration
public class CorsConfig {

    private final List<String> originesAutorisees;

    public CorsConfig(@Value("${app.cors.origines:http://localhost:4200}")
                      List<String> originesAutorisees) {
        this.originesAutorisees = originesAutorisees;
    }

    @Bean
    public CorsConfigurationSource sourceConfigurationCors() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(originesAutorisees);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
