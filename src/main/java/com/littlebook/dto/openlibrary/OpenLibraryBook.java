package com.littlebook.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO représentant un livre dans la réponse OpenLibrary
 * Structure basée sur l'API OpenLibrary: https://openlibrary.org/dev/docs/api/books
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryBook {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("authors")
    private List<OpenLibraryAuthor> authors;
    
    @JsonProperty("publishers")
    private List<OpenLibraryPublisher> publishers;
    
    @JsonProperty("publish_date")
    private String publishDate;
    
    @JsonProperty("number_of_pages")
    private Integer numberOfPages;
    
    @JsonProperty("isbn_10")
    private List<String> isbn10;
    
    @JsonProperty("isbn_13")
    private List<String> isbn13;
    
    @JsonProperty("cover")
    private OpenLibraryCover cover;
    
    @JsonProperty("subjects")
    private List<OpenLibrarySubject> subjects;
    
    // Getters & Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<OpenLibraryAuthor> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<OpenLibraryAuthor> authors) {
        this.authors = authors;
    }
    
    public List<OpenLibraryPublisher> getPublishers() {
        return publishers;
    }
    
    public void setPublishers(List<OpenLibraryPublisher> publishers) {
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
    
    public OpenLibraryCover getCover() {
        return cover;
    }
    
    public void setCover(OpenLibraryCover cover) {
        this.cover = cover;
    }
    
    public List<OpenLibrarySubject> getSubjects() {
        return subjects;
    }
    
    public void setSubjects(List<OpenLibrarySubject> subjects) {
        this.subjects = subjects;
    }
    
    /**
     * Récupère le premier auteur du livre
     */
    public String getFirstAuthorName() {
        if (authors != null && !authors.isEmpty()) {
            return authors.get(0).getName();
        }
        return null;
    }
    
    /**
     * Récupère le premier éditeur du livre
     */
    public String getFirstPublisherName() {
        if (publishers != null && !publishers.isEmpty()) {
            return publishers.get(0).getName();
        }
        return null;
    }
}