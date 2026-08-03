package com.codearena.quiz.dto;

import com.codearena.quiz.QuizStatus;

import java.time.Instant;

public record QuizSummaryResponse(
        String id,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        int durationMinutes,
        int questionCount,
        int totalMarks,
        QuizStatus status,
        boolean attempted,
        Integer userScore
) {
}
