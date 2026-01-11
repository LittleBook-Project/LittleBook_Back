package com.littlebook.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationResponse {
    private UUID id;
    private String userUuid;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(UUID id, String userUuid, String type, String title, String message, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userUuid = userUuid;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getUserUuid() { return userUuid; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
