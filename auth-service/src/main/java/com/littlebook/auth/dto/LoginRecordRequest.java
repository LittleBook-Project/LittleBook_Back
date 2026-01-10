package com.littlebook.auth.dto;

import java.time.LocalDateTime;

public record LoginRecordRequest(
        String provider,
        String ipAddress,
        String userAgent,
        LocalDateTime loggedInAt
) {}
