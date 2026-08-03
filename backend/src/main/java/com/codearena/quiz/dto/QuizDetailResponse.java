package com.codearena.quiz.dto;

import com.codearena.quiz.QuizStatus;

import java.time.Instant;
import java.util.List;

public record QuizDetailResponse(
        String id,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        int durationMinutes,
        int totalMarks,
        QuizStatus status,
        boolean attempted,
        Integer userScore,
        List<QuizQuestionResponse> questions
) {
    public record QuizQuestionResponse(
            String id,
            String questionText,
            List<String> options,
            int points,
            Integer correctOptionIndex // null if student is currently taking quiz
    ) {}
}
