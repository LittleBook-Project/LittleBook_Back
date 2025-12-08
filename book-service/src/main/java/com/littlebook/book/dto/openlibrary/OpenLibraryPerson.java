package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Personne (auteur enrichi) dans OpenLibrary
 */
public class OpenLibraryPerson {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("name")
    private String name;
    
    public OpenLibraryPerson() {}
    
    public OpenLibraryPerson(String key, String name) {
        this.key = key;
        this.name = name;
    }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
