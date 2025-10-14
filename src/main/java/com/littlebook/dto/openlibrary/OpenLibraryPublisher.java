package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO représentant un éditeur dans la réponse OpenLibrary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryPublisher {
    
    @JsonProperty("name")
    private String name;
    
    // Getters & Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}