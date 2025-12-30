package com.littlebook.auth.dto;

import com.littlebook.auth.enums.AuthProvider;

/**
 * DTO pour créer ou synchroniser un utilisateur dans user-service
 */
public record CreateUserRequest(
    AuthProvider provider,
    String providerId,
    String email,
    String name,
    String picture,
    boolean emailVerified
) {}
