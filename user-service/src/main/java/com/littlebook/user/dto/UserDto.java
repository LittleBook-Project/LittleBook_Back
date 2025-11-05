package com.littlebook.user.dto;

/**
 * DTO minimal pour les opérations admin utilisateurs.
 * Utilisation de record (Java 17) pour simplicité.
 */
public record UserDto(Long id, String username, String role) { }
