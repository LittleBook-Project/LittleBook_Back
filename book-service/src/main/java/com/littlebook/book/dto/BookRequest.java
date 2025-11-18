package com.littlebook.book.dto;

/**
 * Requête pour créer ou modifier un livre.
 */
public class BookRequest {

    private String openlibraryId;
    private String isbn10;
    private String isbn13;
    private String title;
    private String subtitle;
    private String authors;
    private Integer publishYear;
    private String coverUrl;
    private String description;
    private String subjects;
    private String sourceData;

    public BookRequest() {}

    // --- Getters / Setters ---

    public String getOpenlibraryId() { return openlibraryId; }
    public void setOpenlibraryId(String openlibraryId) { this.openlibraryId = openlibraryId; }

    public String getIsbn10() { return isbn10; }
    public void setIsbn10(String isbn10) { this.isbn10 = isbn10; }

    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public String getSourceData() { return sourceData; }
    public void setSourceData(String sourceData) { this.sourceData = sourceData; }
}
