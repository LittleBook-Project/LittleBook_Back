package com.littlebook.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_activity")
public class ReviewActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", length = 36)
    private UUID bookId;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ReviewActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
