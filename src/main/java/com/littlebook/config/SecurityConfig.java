package com.littlebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // désactive la protection CSRF (utile pour les requêtes POST via curl)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // permet à tout le monde d’accéder à toutes les routes
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // désactive la popup d'authentification
            .formLogin(form -> form.disable()); // désactive la page de login par défaut

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}