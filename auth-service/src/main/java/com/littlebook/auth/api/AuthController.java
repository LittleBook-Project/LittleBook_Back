package com.littlebook.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth", description = "Endpoints d'authentification et de profil utilisateur")
public class AuthController {

    @Operation(summary = "Récupérer le profil de l'utilisateur connecté",
            description = "Retourne les informations extraites du Firebase ID token (uid, email, name, picture, roles)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur authentifié retourné"),
            @ApiResponse(responseCode = "401", description = "Token manquant ou invalide")
    })
    @Parameter(in = ParameterIn.HEADER, name = "Authorization", description = "Bearer <ID_TOKEN>", required = false)
    @GetMapping("/auth/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        return Map.of(
                "uid", auth.getName(),
                "email", details.get("email"),
                "name", details.get("name"),
                "picture", details.get("picture"),
                "roles", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @Operation(summary = "Ping public",
            description = "Point de vérification public pour tester la disponibilité du service")
    @ApiResponse(responseCode = "200", description = "Service joignable")
    @Parameter(in = ParameterIn.HEADER, name = "Authorization", description = "(optionnel) Bearer <ID_TOKEN>", required = false)
    @GetMapping("/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}