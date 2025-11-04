package com.littlebook.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getRequestURI();
        return p.startsWith("/api/public")
                || p.startsWith("/actuator")
                || "OPTIONS".equalsIgnoreCase(req.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String auth = req.getHeader("Authorization");

            // Pas de Bearer -> ne pas authentifier, laisser Spring décider (renverra 401)
            if (auth == null || !auth.startsWith("Bearer ")) {
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            String token = auth.substring(7);

            // Vérification du token Firebase via bean injecté (mockable en test)
            FirebaseToken decoded = firebaseAuth.verifyIdToken(token);

            var authentication = new UsernamePasswordAuthenticationToken(
                    decoded.getUid(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            authentication.setDetails(Map.of(
                    "email", decoded.getEmail(),
                    "name", decoded.getName(),
                    "picture", decoded.getPicture()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            // Token invalide -> ne pas authentifier ; l'EntryPoint renverra 401
            SecurityContextHolder.clearContext();
        }

        // Toujours poursuivre la chaîne
        chain.doFilter(req, res);
    }
}
