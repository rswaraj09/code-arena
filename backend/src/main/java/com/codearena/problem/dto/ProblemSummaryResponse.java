package com.codearena.problem.dto;

import com.codearena.problem.Difficulty;

import java.util.List;

public record ProblemSummaryResponse(
        String id,
        String title,
        String slug,
        Difficulty difficulty,
        List<String> tags,
        boolean solvedByCurrentUser,
        double acceptanceRate
) {
}
