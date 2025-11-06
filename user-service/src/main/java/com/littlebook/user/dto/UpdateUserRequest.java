package com.littlebook.user.dto;

/** DTO pour mise à jour partielle du profil utilisateur */
public record UpdateUserRequest(
    String name,
    String picture,
    String roles
) {}
