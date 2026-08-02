package com.codearena.submission.dto;

import com.codearena.submission.Language;
import com.codearena.submission.Verdict;

import java.time.Instant;

public record SubmissionHistoryResponse(
        String id,
        Language language,
        Verdict verdict,
        Long runtimeMs,
        Instant submittedAt
) {
}
