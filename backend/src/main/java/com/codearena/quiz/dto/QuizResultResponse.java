package com.codearena.quiz.dto;

import java.time.Instant;
import java.util.List;

public record QuizResultResponse(
        String attemptId,
        String quizId,
        String quizTitle,
        int score,
        int totalMarks,
        double percentage,
        boolean completed,
        Instant submittedAt,
        List<QuestionResult> questionResults
) {
    public record QuestionResult(
            String questionId,
            String questionText,
            List<String> options,
            Integer selectedOptionIndex,
            int correctOptionIndex,
            boolean isCorrect,
            int pointsEarned,
            int maxPoints
    ) {}
}
