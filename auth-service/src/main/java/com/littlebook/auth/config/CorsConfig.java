package com.littlebook.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration CORS pour auth-service.
 *
 * Approche choisie : source unique de vérité via un bean CorsConfigurationSource
 * qui sera utilisé par Spring Security (SecurityConfig doit activer http.cors()).
 */
@Configuration
public class CorsConfig {

    private final CorsProperties props;

    public CorsConfig(CorsProperties props) {
        this.props = props;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

    // origines (liste explicite - NE PAS utiliser "*" lorsque allowCredentials=true)
        config.setAllowedOrigins(props.getAllowedOrigins());

    // méthodes autorisées
        List<String> methods = props.getAllowedMethods();
        config.setAllowedMethods(methods != null ? methods : List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // en-têtes autorisés
        List<String> headers = props.getAllowedHeaders();
        config.setAllowedHeaders(headers != null ? headers : List.of("Authorization", "Content-Type", "X-Requested-With"));

    // en-têtes exposés (liste explicite requise par les navigateurs quand credentials=true)
        List<String> exposed = props.getExposedHeaders();
        config.setExposedHeaders(exposed != null ? exposed : List.of("Authorization", "Content-Type", "Location"));

        config.setAllowCredentials(true);
        config.setMaxAge(props.getMaxAge() != null ? props.getMaxAge() : 3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
