package com.codearena.problem.dto;

import com.codearena.problem.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateProblemRequest(
        @NotBlank String title,
        @NotNull Difficulty difficulty,
        @NotBlank String description,
        String constraints,
        List<String> tags,
        List<String> hints,
        Integer timeLimitMs,
        Integer memoryLimitMb,
        @NotEmpty @Valid List<TestCaseRequest> testCases
) {
}
