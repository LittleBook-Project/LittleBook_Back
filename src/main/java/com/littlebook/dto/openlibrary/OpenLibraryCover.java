package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO représentant la couverture d'un livre dans la réponse OpenLibrary
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryCover {
    
    @JsonProperty("small")
    private String small;
    
    @JsonProperty("medium")
    private String medium;
    
    @JsonProperty("large")
    private String large;
    
    // Getters & Setters
    public String getSmall() {
        return small;
    }
    
    public void setSmall(String small) {
        this.small = small;
    }
    
    public String getMedium() {
        return medium;
    }
    
    public void setMedium(String medium) {
        this.medium = medium;
    }
    
    public String getLarge() {
        return large;
    }
    
    public void setLarge(String large) {
        this.large = large;
    }
    
    /**
     * Récupère la meilleure image disponible (large > medium > small)
     */
    public String getBestCoverUrl() {
        if (large != null && !large.isEmpty()) {
            return large;
        }
        if (medium != null && !medium.isEmpty()) {
            return medium;
        }
        return small;
    }
}