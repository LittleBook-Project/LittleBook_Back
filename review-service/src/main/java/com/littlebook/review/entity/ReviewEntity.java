package com.littlebook.review.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "review")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "review_creation_date")
    private LocalDate reviewCreationDate = LocalDate.now();

    private Integer rating;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "book_isbn")
    private String bookIsbn;

    @Column(name = "book_id")
    private String bookId;

    // Getters / setters

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
