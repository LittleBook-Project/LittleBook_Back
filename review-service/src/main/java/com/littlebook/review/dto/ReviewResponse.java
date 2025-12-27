package com.littlebook.review.dto;

import java.time.LocalDate;

public class ReviewResponse {

    private Long id;
    private String description;
    private LocalDate reviewCreationDate;
    private Integer rating;
    private String userUuid;
    private String bookIsbn;
    private String bookId;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id,
                          String description,
                          LocalDate reviewCreationDate,
                          Integer rating,
                          String userUuid,
                          String bookIsbn,
                          String bookId) {
        this.id = id;
        this.description = description;
        this.reviewCreationDate = reviewCreationDate;
        this.rating = rating;
        this.userUuid = userUuid;
        this.bookIsbn = bookIsbn;
        this.bookId = bookId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReviewCreationDate() { return reviewCreationDate; }
    public void setReviewCreationDate(LocalDate reviewCreationDate) { this.reviewCreationDate = reviewCreationDate; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
}
