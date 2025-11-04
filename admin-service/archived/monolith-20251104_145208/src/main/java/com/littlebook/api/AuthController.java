package com.littlebook.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

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

    @GetMapping("/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}