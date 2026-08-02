package com.codearena.submission.judge;

import com.codearena.submission.Language;

public interface JudgeService {

    /**
     * Compiles (if needed) and runs {@code code} against a single input
     * inside a sandboxed, resource-limited Docker container.
     *
     * @return a result whose verdict is either an execution-level failure
     * (COMPILATION_ERROR, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR) or PENDING —
     * meaning the code ran to completion and the caller still needs to
     * compare {@code stdout} against the expected output to decide
     * ACCEPTED / WRONG_ANSWER / PRESENTATION_ERROR.
     */
    JudgeResult execute(Language language, String code, String stdin, int timeLimitMs, int memoryLimitMb);
}
