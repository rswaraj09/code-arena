package com.codearena.problem.dto;

import com.codearena.problem.Difficulty;

import java.util.List;

public record ProblemDetailResponse(
        String id,
        String title,
        String slug,
        Difficulty difficulty,
        String description,
        String constraints,
        List<String> tags,
        List<String> hints,
        List<ExampleResponse> examples,
        Integer timeLimitMs,
        Integer memoryLimitMb
) {
}
