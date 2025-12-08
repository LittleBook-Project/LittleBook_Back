package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lieu (endroit) dans OpenLibrary
 */
public class OpenLibraryPlace {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("name")
    private String name;
    
    public OpenLibraryPlace() {}
    
    public OpenLibraryPlace(String key, String name) {
        this.key = key;
        this.name = name;
    }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
