package com.littlebook.admin.dto;

import java.time.LocalDateTime;

public class SetLastLoginRequest {
    private LocalDateTime lastLoginAt;

    public SetLastLoginRequest() {}

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
