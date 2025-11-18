package com.littlebook.user.dto;

import com.littlebook.user.enums.AuthProvider;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String name,
    String picture,
    AuthProvider provider,
    String providerId,
    boolean emailVerified,
    String roles,
    Instant createdAt,
    Instant lastLoginAt,
    Instant updatedAt,
    boolean isActive
) {}
