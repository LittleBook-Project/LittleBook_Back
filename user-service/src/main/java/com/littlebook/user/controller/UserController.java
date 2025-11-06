package com.littlebook.user.controller;

import com.littlebook.user.dto.CreateUserRequest;
import com.littlebook.user.dto.UpdateUserRequest;
import com.littlebook.user.dto.UserResponse;
import com.littlebook.user.entity.User;
import com.littlebook.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public String health() { return "user-service OK"; }

    @GetMapping("/ping")
    public String ping() { return userService.ping(); }

    @PostMapping("/oauth")
    public ResponseEntity<UserResponse> oauthLogin(@RequestBody CreateUserRequest req) {
        // Endpoint utilisé par l'auth-service après échange OAuth
        User u = userService.getOrCreateFromOAuth(req);
        return ResponseEntity.ok(toResponse(u));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
        try {
            User updated = userService.updateProfile(id, req);
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getPicture(),
                u.getProvider(),
                u.getProviderId(),
                u.isEmailVerified(),
                u.getRoles(),
                u.getCreatedAt(),
                u.getLastLogin(),
                u.getUpdatedAt(),
                u.isActive()
        );
    }
}
