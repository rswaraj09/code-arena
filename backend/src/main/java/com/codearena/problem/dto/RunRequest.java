package com.codearena.problem.dto;

import com.codearena.submission.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RunRequest(
        @NotBlank String code,
        @NotNull Language language,
        String customInput
) {
}
