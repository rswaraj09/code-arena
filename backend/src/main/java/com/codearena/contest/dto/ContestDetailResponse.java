package com.codearena.contest.dto;

import com.codearena.contest.ContestStatus;
import com.codearena.problem.dto.ProblemSummaryResponse;

import java.time.Instant;
import java.util.List;

public record ContestDetailResponse(
        String id,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        boolean negativeMarking,
        ContestStatus status,
        List<ProblemSummaryResponse> problems,
        boolean currentUserRegistered
) {
}
