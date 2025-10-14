package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO représentant la réponse complète d'OpenLibrary pour un livre
 * La réponse est un Map avec l'ISBN comme clé
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryBookResponse {
    
    @JsonProperty
    private Map<String, OpenLibraryBook> books;
    
    // Getters & Setters
    public Map<String, OpenLibraryBook> getBooks() {
        return books;
    }
    
    public void setBooks(Map<String, OpenLibraryBook> books) {
        this.books = books;
    }
    
    /**
     * Récupère le premier livre de la réponse (utile quand on cherche par ISBN unique)
     */
    public OpenLibraryBook getFirstBook() {
        if (books != null && !books.isEmpty()) {
            return books.values().iterator().next();
        }
        return null;
    }
    
    /**
     * Ajoute un livre à la réponse
     */
    public void addBook(String key, OpenLibraryBook book) {
        if (this.books == null) {
            this.books = new HashMap<>();
        }
        this.books.put(key, book);
    }
}