package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Auteur dans les résultats OpenLibrary
 */
public class OpenLibraryAuthor {
    
    @JsonProperty("key")
    private String key;                    // ex: "/authors/OL34221A"
    
    @JsonProperty("name")
    private String name;
    
    public OpenLibraryAuthor() {}
    
    public OpenLibraryAuthor(String key, String name) {
        this.key = key;
        this.name = name;
    }
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    /**
     * Extrait l'ID OpenLibrary (ex: "OL34221A" depuis "/authors/OL34221A")
     */
    public String getOpenLibraryId() {
        if (key != null && key.contains("/")) {
            return key.substring(key.lastIndexOf("/") + 1);
        }
        return key;
    }
}
