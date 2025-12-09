package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Document (livre) dans la réponse OpenLibrary
 * Représente un livre trouvé via l'API
 */
public class OpenLibraryBook {
    
    @JsonProperty("key")
    private String key;                    // ex: "/works/OL45883W"
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("subtitle")
    private String subtitle;
    
    @JsonProperty("authors")
    private List<OpenLibraryAuthor> authors;
    
    @JsonProperty("first_publish_year")
    private Integer firstPublishYear;
    
    @JsonProperty("isbn")
    private List<String> isbn;             // ISBN-10
    
    @JsonProperty("isbn13")
    private List<String> isbn13;
    
    @JsonProperty("cover_id")
    private Integer coverId;
    
    @JsonProperty("first_edition_description")
    private String description;
    
    @JsonProperty("subject")
    private List<String> subjects;
    
    @JsonProperty("person")
    private List<OpenLibraryPerson> persons;
    
    @JsonProperty("place")
    private List<OpenLibraryPlace> places;
    
    @JsonProperty("time")
    private List<String> times;
    
    @JsonProperty("edition_count")
    private Integer editionCount;
    
    @JsonProperty("has_fulltext")
    private Boolean hasFulltext;
    
    public OpenLibraryBook() {}
    
    // Getters / Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    
    public List<OpenLibraryAuthor> getAuthors() { return authors; }
    public void setAuthors(List<OpenLibraryAuthor> authors) { this.authors = authors; }
    
    public Integer getFirstPublishYear() { return firstPublishYear; }
    public void setFirstPublishYear(Integer firstPublishYear) { this.firstPublishYear = firstPublishYear; }
    
    public List<String> getIsbn() { return isbn; }
    public void setIsbn(List<String> isbn) { this.isbn = isbn; }
    
    public List<String> getIsbn13() { return isbn13; }
    public void setIsbn13(List<String> isbn13) { this.isbn13 = isbn13; }
    
    public Integer getCoverId() { return coverId; }
    public void setCoverId(Integer coverId) { this.coverId = coverId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    
    public List<OpenLibraryPerson> getPersons() { return persons; }
    public void setPersons(List<OpenLibraryPerson> persons) { this.persons = persons; }
    
    public List<OpenLibraryPlace> getPlaces() { return places; }
    public void setPlaces(List<OpenLibraryPlace> places) { this.places = places; }
    
    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }
    
    public Integer getEditionCount() { return editionCount; }
    public void setEditionCount(Integer editionCount) { this.editionCount = editionCount; }
    
    public Boolean getHasFulltext() { return hasFulltext; }
    public void setHasFulltext(Boolean hasFulltext) { this.hasFulltext = hasFulltext; }
    
    /**
     * Extrait l'ID OpenLibrary (ex: "OL45883W" depuis "/works/OL45883W")
     */
    public String getOpenLibraryId() {
        if (key != null && key.contains("/")) {
            return key.substring(key.lastIndexOf("/") + 1);
        }
        return key;
    }
    
    /**
     * Construit l'URL de la couverture
     * Format: https://covers.openlibrary.org/b/id/{coverId}-M.jpg
     */
    public String getCoverUrl() {
        if (coverId != null) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }
    
    /**
     * Retourne les noms des auteurs en une seule chaîne (ex: "Author1, Author2")
     */
    public String getAuthorsAsString() {
        if (authors == null || authors.isEmpty()) {
            return null;
        }
        return String.join(", ", authors.stream()
                .map(OpenLibraryAuthor::getName)
                .toList());
    }
    
    /**
     * Retourne les sujets en une seule chaîne (ex: "Subject1, Subject2")
     */
    public String getSubjectsAsString() {
        if (subjects == null || subjects.isEmpty()) {
            return null;
        }
        return String.join(", ", subjects);
    }
}
