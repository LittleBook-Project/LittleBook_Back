package com.littlebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // désactiver CSRF pour tests via curl/postman (si pas de cookies côté navigateur)
            .csrf(csrf -> csrf.disable())

            // règles d'autorisation
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register").permitAll()   // autorise l'inscription publique
                .requestMatchers("/api/users/**").permitAll()         // si tu veux pouvoir GET sans auth (optionnel)
                .requestMatchers("/actuator/**").permitAll()          // utile pour health checks
                .anyRequest().authenticated()
            )

            // pas de session côté serveur (stateless pour API)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // garde http basic 
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
