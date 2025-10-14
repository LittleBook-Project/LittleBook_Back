package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO représentant un sujet/catégorie d'un livre dans la réponse OpenLibrary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibrarySubject {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("key")
    private String key;
    
    // Getters & Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
}