package com.littlebook.auth.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
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
    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    // Fournisseurs autorisés - accepter les connexions fédérées Google et Microsoft
    private static final Set<String> ALLOWED_PROVIDERS = Set.of("google.com", "microsoft.com");

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

            // Extraire provider et infos utiles depuis les claims pour debug et règles métiers
            String provider = null;
            try {
                Object firebaseClaim = decoded.getClaims().get("firebase");
                if (firebaseClaim instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> firebaseMap = (Map<String, Object>) firebaseClaim;
                    Object p = firebaseMap.get("sign_in_provider");
                    if (p != null) provider = p.toString();
                }
            } catch (Exception e) {
                // Non fatal - nous loguons mais ne bloquons pas ici
                log.debug("Could not read firebase.sign_in_provider claim", e);
            }

            String email = decoded.getEmail();
            Boolean emailVerified = decoded.isEmailVerified();

            log.info("Firebase login: provider={}, email={}, verified={}", provider, email, emailVerified);

            // Si le fournisseur est présent et non autorisé, ne pas authentifier
            if (provider != null && !ALLOWED_PROVIDERS.contains(provider)) {
                log.warn("Rejecting login from unauthorized provider {}", provider);
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            // Pour "microsoft.com", autoriser même si emailVerified == false (loguer un avertissement)
            if ("microsoft.com".equals(provider) && Boolean.FALSE.equals(emailVerified)) {
                log.warn("Microsoft login with unverified email for {} - accepting authentication", email);
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    decoded.getUid(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Construire la map des détails de manière sécurisée vis-à-vis des null : HashMap accepte
            // des valeurs nulles, mais on préfère n'inclure que les champs présents pour limiter
            // la taille de la payload.
            Map<String, Object> details = new HashMap<>();
            if (email != null) details.put("email", email);
            if (decoded.getName() != null) details.put("name", decoded.getName());
            if (decoded.getPicture() != null) details.put("picture", decoded.getPicture());
            if (provider != null) details.put("provider", provider);

            authentication.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            // Token invalide -> ne pas authentifier ; l'EntryPoint renverra 401
            log.warn("Firebase token verification failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Toujours poursuivre la chaîne
        chain.doFilter(req, res);
    }
}
