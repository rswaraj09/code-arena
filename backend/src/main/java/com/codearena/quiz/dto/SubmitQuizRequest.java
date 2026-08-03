package com.codearena.quiz.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitQuizRequest(
        @NotNull Map<String, Integer> answers
) {
}
