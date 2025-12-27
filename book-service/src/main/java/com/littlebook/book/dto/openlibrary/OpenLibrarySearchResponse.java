package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Réponse de recherche OpenLibrary API
 * Format: https://openlibrary.org/api/books?title=Harry+Potter
 */
public class OpenLibrarySearchResponse {
    
    @JsonProperty("numFound")
    private int numFound;
    
    @JsonProperty("start")
    private int start;
    
    @JsonProperty("numFoundExact")
    private boolean numFoundExact;
    
    @JsonProperty("docs")
    private List<OpenLibraryBook> docs;
    
    public OpenLibrarySearchResponse() {}
    
    public OpenLibrarySearchResponse(int numFound, int start, boolean numFoundExact, List<OpenLibraryBook> docs) {
        this.numFound = numFound;
        this.start = start;
        this.numFoundExact = numFoundExact;
        this.docs = docs;
    }
    
    // Getters / Setters
    public int getNumFound() {
        return numFound;
    }
    
    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public boolean isNumFoundExact() {
        return numFoundExact;
    }
    
    public void setNumFoundExact(boolean numFoundExact) {
        this.numFoundExact = numFoundExact;
    }
    
    public List<OpenLibraryBook> getDocs() {
        return docs;
    }
    
    public void setDocs(List<OpenLibraryBook> docs) {
        this.docs = docs;
    }
}
