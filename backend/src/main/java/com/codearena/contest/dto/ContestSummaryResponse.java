package com.codearena.contest.dto;

import com.codearena.contest.ContestStatus;

import java.time.Instant;

public record ContestSummaryResponse(
        String id,
        String title,
        Instant startTime,
        Instant endTime,
        ContestStatus status,
        int problemCount,
        long participantCount
) {
}
