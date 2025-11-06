package com.littlebook.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_login_stats")
public class UserLoginStats {

    @Id
    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "total_logins")
    private int totalLogins;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "first_login_at")
    private LocalDateTime firstLoginAt;

    @Column(name = "avg_days_between_logins")
    private double avgDaysBetweenLogins;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserLoginStats() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(int totalLogins) {
        this.totalLogins = totalLogins;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getFirstLoginAt() {
        return firstLoginAt;
    }

    public void setFirstLoginAt(LocalDateTime firstLoginAt) {
        this.firstLoginAt = firstLoginAt;
    }

    public double getAvgDaysBetweenLogins() {
        return avgDaysBetweenLogins;
    }

    public void setAvgDaysBetweenLogins(double avgDaysBetweenLogins) {
        this.avgDaysBetweenLogins = avgDaysBetweenLogins;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
