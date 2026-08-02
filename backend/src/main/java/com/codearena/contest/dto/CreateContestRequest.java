package com.codearena.contest.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CreateContestRequest(
        @NotBlank String title,
        String description,
        @NotNull @Future Instant startTime,
        @NotNull Instant endTime,
        boolean negativeMarking,
        @NotEmpty List<String> problemIds
) {
}
