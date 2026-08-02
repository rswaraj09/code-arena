package com.codearena.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record TestCaseRequest(
        @NotBlank String input,
        @NotBlank String expectedOutput,
        boolean hidden,
        Integer weight
) {
}
