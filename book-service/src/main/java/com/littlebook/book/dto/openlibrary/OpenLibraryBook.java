package com.littlebook.book.dto.openlibrary;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

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

    // Présent dans certaines réponses (ex: search editions)
    @JsonProperty("author_name")
    private List<String> authorNames;
    
    @JsonProperty("first_publish_year")
    private Integer firstPublishYear;

    // Peut être fourni comme liste (editions)
    @JsonProperty("publish_year")
    private List<Integer> publishYears;
    
    @JsonProperty("isbn")
    private List<String> isbn;             // ISBN-10
    
    @JsonProperty("isbn13")
    private List<String> isbn13;
    
    @JsonProperty("cover_id")
    private Integer coverId;

    // Présent sur l'API /works/{id}.json
    @JsonProperty("covers")
    private List<Integer> covers;

    // Présent dans la recherche (cover_i)
    @JsonProperty("cover_i")
    private Integer coverI;
    
    // Peut être une string ou un objet {"type":"text","value":"..."}
    @JsonProperty("first_edition_description")
    private String description;

    @JsonProperty("description")
    private Object descriptionRaw;
    
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

    public List<String> getAuthorNames() { return authorNames; }
    public void setAuthorNames(List<String> authorNames) { this.authorNames = authorNames; }
    
    public Integer getFirstPublishYear() { return firstPublishYear; }
    public void setFirstPublishYear(Integer firstPublishYear) { this.firstPublishYear = firstPublishYear; }

    public List<Integer> getPublishYears() { return publishYears; }
    public void setPublishYears(List<Integer> publishYears) { this.publishYears = publishYears; }
    
    public List<String> getIsbn() { return isbn; }
    public void setIsbn(List<String> isbn) { this.isbn = isbn; }
    
    public List<String> getIsbn13() { return isbn13; }
    public void setIsbn13(List<String> isbn13) { this.isbn13 = isbn13; }
    
    public Integer getCoverId() { return coverId; }
    public void setCoverId(Integer coverId) { this.coverId = coverId; }

    public List<Integer> getCovers() { return covers; }
    public void setCovers(List<Integer> covers) { this.covers = covers; }

    public Integer getCoverI() { return coverI; }
    public void setCoverI(Integer coverI) { this.coverI = coverI; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Object getDescriptionRaw() { return descriptionRaw; }
    public void setDescriptionRaw(Object descriptionRaw) { this.descriptionRaw = descriptionRaw; }
    
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
        Integer id = coverId;
        if (id == null && covers != null && !covers.isEmpty()) {
            id = covers.get(0);
        }
        if (id == null && coverI != null) {
            id = coverI;
        }
        if (id != null) {
            return "https://covers.openlibrary.org/b/id/" + id + "-M.jpg";
        }
        return null;
    }

    /**
     * Retourne une description normalisée (string), gère le cas où description est un objet { value: "..." }
     */
    public String getDescriptionNormalized() {
        if (description != null && !description.isBlank()) {
            return description;
        }
        if (descriptionRaw instanceof String s) {
            return s;
        }
        if (descriptionRaw instanceof Map<?, ?> map) {
            Object value = map.get("value");
            if (value instanceof String v) {
                return v;
            }
        }
        return null;
    }
    
    /**
     * Retourne les noms des auteurs en une seule chaîne (ex: "Author1, Author2")
     */
    public String getAuthorsAsString() {
        if (authors != null && !authors.isEmpty()) {
            return String.join(", ", authors.stream()
                    .map(OpenLibraryAuthor::getName)
                    .toList());
        }
        if (authorNames != null && !authorNames.isEmpty()) {
            return String.join(", ", authorNames);
        }
        return null;
    }

    /**
     * Retourne une année de publication en fallback depuis publishYears
     */
    public Integer getPublishYearNormalized() {
        if (firstPublishYear != null) return firstPublishYear;
        if (publishYears != null && !publishYears.isEmpty()) {
            return publishYears.get(0);
        }
        return null;
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
