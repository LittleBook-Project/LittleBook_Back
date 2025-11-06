package com.littlebook.user.dto;

import com.littlebook.user.enums.AuthProvider;

/**
 * DTO utilisé par l'auth-service pour provisionner ou synchroniser un profil
 */
public record CreateUserRequest(
    AuthProvider provider,
    String providerId,
    String email,
    String name,
    String picture,
    boolean emailVerified
) {}
