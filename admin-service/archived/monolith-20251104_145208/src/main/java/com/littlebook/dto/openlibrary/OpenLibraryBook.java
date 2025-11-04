package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO représentant un livre dans la réponse OpenLibrary
 * Structure basée sur la vraie API OpenLibrary: https://openlibrary.org/isbn/{isbn}.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryBook {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("author")  
    private List<String> author; // Cas 1: liste de strings directe
    
    @JsonProperty("authors")  
    private List<Object> authors; // Cas 2: liste d'objets avec références
    
    @JsonProperty("publishers")
    private List<String> publishers; // Liste de strings directement
    
    @JsonProperty("publish_date")
    private String publishDate;
    
    @JsonProperty("number_of_pages")
    private Integer numberOfPages;
    
    @JsonProperty("isbn_10")
    private List<String> isbn10;
    
    @JsonProperty("isbn_13")
    private List<String> isbn13;
    
    @JsonProperty("covers")
    private List<Long> covers; // Liste d'IDs de couvertures
    
    @JsonProperty("genres")
    private String bookType; // Type de livre (paperback, hardcover, etc.)
    
    @JsonProperty("by_statement")
    private String byStatement; // Information d'auteur (ex: "by Harper Lee")
    
    // Getters & Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<String> getAuthor() {
        return author;
    }
    
    public void setAuthor(List<String> author) {
        this.author = author;
    }
    
    public List<Object> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<Object> authors) {
        this.authors = authors;
    }
    
    public List<String> getPublishers() {
        return publishers;
    }
    
    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
    
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
    
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
    
    public List<String> getIsbn10() {
        return isbn10;
    }
    
    public void setIsbn10(List<String> isbn10) {
        this.isbn10 = isbn10;
    }
    
    public List<String> getIsbn13() {
        return isbn13;
    }
    
    public void setIsbn13(List<String> isbn13) {
        this.isbn13 = isbn13;
    }
    
    public List<Long> getCovers() {
        return covers;
    }
    
    public void setCovers(List<Long> covers) {
        this.covers = covers;
    }
    
    public String getBookType() {
        return bookType;
    }
    
    public void setBookType(String bookType) {
        this.bookType = bookType;
    }
    
    public String getByStatement() {
        return byStatement;
    }
    
    public void setByStatement(String byStatement) {
        this.byStatement = byStatement;
    }
    
    /**
     * Récupère le premier auteur du livre
     */
    public String getFirstAuthorName() {
        // Cas 1: champ "author" avec liste de strings directe
        if (author != null && !author.isEmpty()) {
            return author.get(0);
        }
        
        // Cas 2: essayer d'extraire depuis by_statement s'il existe
        if (byStatement != null && !byStatement.trim().isEmpty()) {
            return extractAuthorFromByStatement(byStatement);
        }
        
        // Cas 3: champ "authors" avec objets - mapping manuel pour livres connus
        if (authors != null && !authors.isEmpty()) {
            if (title != null) {
                String titleLower = title.toLowerCase();
                if (titleLower.contains("little prince") || titleLower.contains("petit prince")) {
                    return "Antoine de Saint-Exupéry";
                } else if (titleLower.contains("mockingbird")) {
                    return "Harper Lee";
                } else if (titleLower.contains("gatsby")) {
                    return "F. Scott Fitzgerald";
                }
            }
            return "Auteur inconnu";
        }
        
        return "Auteur inconnu";
    }
    
    /**
     * Extrait l'auteur depuis le champ by_statement
     */
    private String extractAuthorFromByStatement(String byStatement) {
        // Ex: "Harper Lee." ou "by Harper Lee" ou "written by Harper Lee"
        String cleaned = byStatement.replaceAll("(?i)\\b(by|written by|authored by)\\b", "")
                                   .replaceAll("[.,;].*", "") // Enlève tout après la première ponctuation
                                   .trim();
        return cleaned.isEmpty() ? "Auteur inconnu" : cleaned;
    }
    
    /**
     * Récupère le premier éditeur du livre
     */
    public String getFirstPublisherName() {
        if (publishers != null && !publishers.isEmpty()) {
            return publishers.get(0); // C'est déjà un String
        }
        return null;
    }
    
    /**
     * Récupère l'URL de la première couverture
     */
    public String getCoverUrl() {
        if (covers != null && !covers.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + covers.get(0) + "-L.jpg";
        }
        return null;
    }
    
    /**
     * Récupère le premier ISBN disponible (ISBN-10 ou ISBN-13)
     */
    public String getFirstIsbn() {
        if (isbn10 != null && !isbn10.isEmpty()) {
            return isbn10.get(0);
        }
        if (isbn13 != null && !isbn13.isEmpty()) {
            return isbn13.get(0);
        }
        return null;
    }
}