package com.littlebook.entity;

import jakarta.persistence.*;
import com.littlebook.enums.ReadingStatus;
import java.time.LocalDate;

@Entity
@Table(name = "reading")
public class ReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reading_date")
    private LocalDate readingDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false, referencedColumnName = "uuid")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false, referencedColumnName = "isbn")
    private BookEntity book;

    // Getters & Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }
    public void setReadingDate(LocalDate readingDate) {
        this.readingDate = readingDate;
    }

    public ReadingStatus getStatus() {
        return status;
    }
    public void setStatus(ReadingStatus status) {
        this.status = status;
    }

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }
    public void setBook(BookEntity book) {
        this.book = book;
    }
}
