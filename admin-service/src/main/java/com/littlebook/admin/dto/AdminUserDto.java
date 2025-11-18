package com.littlebook.admin.dto;

/**
 * DTO minimal pour les opérations admin utilisateurs.
 * Utilisation de record (Java 17) pour simplicité.
 */
public record AdminUserDto(Long id, String username, String role) { }
