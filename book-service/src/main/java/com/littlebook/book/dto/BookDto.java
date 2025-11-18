package com.littlebook.book.dto;

/**
 * DTO minimal pour les opérations admin utilisateurs.
 * Utilisation de record (Java 17) pour simplicité.
 */
public record BookDto(Long id, String title, String author) { }
