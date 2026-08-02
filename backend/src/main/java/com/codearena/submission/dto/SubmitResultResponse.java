package com.codearena.submission.dto;

import com.codearena.submission.Verdict;

public record SubmitResultResponse(
        String submissionId,
        Verdict verdict,
        int testCasesPassed,
        int testCasesTotal,
        long runtimeMs,
        String failingOutput
) {
}
