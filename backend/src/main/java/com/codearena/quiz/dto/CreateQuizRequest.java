package com.codearena.quiz.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateQuizRequest(
        @NotBlank String title,
        String description,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Min(1) int durationMinutes,
        @NotEmpty List<QuestionDto> questions
) {
    public record QuestionDto(
            @NotBlank String questionText,
            @NotEmpty List<String> options,
            @Min(0) int correctOptionIndex,
            @Min(1) int points
    ) {}
}
