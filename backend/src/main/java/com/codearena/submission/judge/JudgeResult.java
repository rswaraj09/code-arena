package com.codearena.submission.judge;

import com.codearena.submission.Verdict;

public record JudgeResult(
        Verdict verdict,
        String stdout,
        String stderr,
        long runtimeMs,
        long memoryKb
) {
    public static JudgeResult of(Verdict verdict, String stdout, String stderr, long runtimeMs) {
        return new JudgeResult(verdict, stdout, stderr, runtimeMs, 0L);
    }
}
