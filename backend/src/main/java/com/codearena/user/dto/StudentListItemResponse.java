package com.codearena.user.dto;

import java.time.Instant;

public record StudentListItemResponse(
        String id,
        String name,
        String email,
        String college,
        String year,
        String branch,
        boolean emailVerified,
        long solvedCount,
        long totalSubmissions,
        Instant createdAt
) {
}
