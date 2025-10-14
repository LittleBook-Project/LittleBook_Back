package com.littlebook.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                // API stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // Gestion claire des erreurs de sécu (401 vs 403)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)) // 401
                                                                                                                        // si
                                                                                                                        // non
                                                                                                                        // authentifié
                                                                                                                        // /
                                                                                                                        // token
                                                                                                                        // invalide
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN)) // 403
                                                                                                                // si
                                                                                                                // authentifié
                                                                                                                // mais
                                                                                                                // pas
                                                                                                                // autorisé
                )

                // Règles d'accès
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // préflight CORS
                        .requestMatchers("/api/public/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated())

                // Vérifie le Bearer Firebase AVANT UsernamePasswordAuthenticationFilter
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
