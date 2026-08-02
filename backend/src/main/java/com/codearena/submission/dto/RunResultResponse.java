package com.codearena.submission.dto;

import com.codearena.submission.Verdict;

public record RunResultResponse(
        Verdict verdict,
        String stdout,
        String stderr,
        long runtimeMs
) {
}
