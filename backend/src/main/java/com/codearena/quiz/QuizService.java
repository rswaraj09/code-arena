package com.codearena.quiz;

import com.codearena.common.exception.BadRequestException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.quiz.dto.*;
import com.codearena.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public Quiz create(CreateQuizRequest request, User creator) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Test end time must be after the start time.");
        }

        List<QuizQuestion> questions = new ArrayList<>();
        int totalMarks = 0;

        for (CreateQuizRequest.QuestionDto q : request.questions()) {
            String questionId = UUID.randomUUID().toString();
            int points = Math.max(1, q.points());
            totalMarks += points;

            questions.add(QuizQuestion.builder()
                    .id(questionId)
                    .questionText(q.questionText())
                    .options(q.options())
                    .correctOptionIndex(q.correctOptionIndex())
                    .points(points)
                    .build());
        }

        Quiz quiz = Quiz.builder()
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .durationMinutes(request.durationMinutes())
                .totalMarks(totalMarks)
                .createdById(creator.getId())
                .questions(questions)
                .build();

        return quizRepository.save(quiz);
    }

    public List<QuizSummaryResponse> list(User currentUser) {
        List<Quiz> quizzes = quizRepository.findAllByOrderByCreatedAtDesc();
        String userId = currentUser != null ? currentUser.getId() : null;

        return quizzes.stream()
                .map(q -> {
                    boolean attempted = false;
                    Integer score = null;
                    if (userId != null) {
                        Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findByQuizIdAndUserId(q.getId(), userId);
                        if (attemptOpt.isPresent()) {
                            attempted = true;
                            score = attemptOpt.get().getScore();
                        }
                    }
                    return new QuizSummaryResponse(
                            q.getId(),
                            q.getTitle(),
                            q.getDescription(),
                            q.getStartTime(),
                            q.getEndTime(),
                            q.getDurationMinutes(),
                            q.getQuestions().size(),
                            q.getTotalMarks(),
                            q.getStatus(),
                            attempted,
                            score
                    );
                })
                .toList();
    }

    public QuizDetailResponse getDetail(String quizId, User currentUser) {
        Quiz quiz = getById(quizId);
        String userId = currentUser != null ? currentUser.getId() : null;

        boolean attempted = false;
        Integer score = null;
        if (userId != null) {
            Optional<QuizAttempt> attemptOpt = quizAttemptRepository.findByQuizIdAndUserId(quizId, userId);
            if (attemptOpt.isPresent()) {
                attempted = true;
                score = attemptOpt.get().getScore();
            }
        }

        // Show answer keys only if user created the quiz or user already attempted it
        boolean showAnswers = attempted || (currentUser != null && currentUser.getId().equals(quiz.getCreatedById()));

        List<QuizDetailResponse.QuizQuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> new QuizDetailResponse.QuizQuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getOptions(),
                        q.getPoints(),
                        showAnswers ? q.getCorrectOptionIndex() : null
                ))
                .toList();

        return new QuizDetailResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getStartTime(),
                quiz.getEndTime(),
                quiz.getDurationMinutes(),
                quiz.getTotalMarks(),
                quiz.getStatus(),
                attempted,
                score,
                questionResponses
        );
    }

    public QuizResultResponse submit(String quizId, SubmitQuizRequest request, User student) {
        Quiz quiz = getById(quizId);

        if (quiz.getStatus() == QuizStatus.UPCOMING) {
            throw new BadRequestException("This test has not started yet.");
        }
        if (quiz.getStatus() == QuizStatus.ENDED) {
            throw new BadRequestException("This test has already ended.");
        }

        if (quizAttemptRepository.existsByQuizIdAndUserId(quizId, student.getId())) {
            throw new BadRequestException("You have already submitted this test.");
        }

        Map<String, Integer> userAnswers = request.answers() != null ? request.answers() : Map.of();
        int totalScore = 0;
        List<QuizResultResponse.QuestionResult> questionResults = new ArrayList<>();

        for (QuizQuestion q : quiz.getQuestions()) {
            Integer selectedOpt = userAnswers.get(q.getId());
            boolean isCorrect = selectedOpt != null && selectedOpt == q.getCorrectOptionIndex();
            int pointsEarned = isCorrect ? q.getPoints() : 0;
            totalScore += pointsEarned;

            questionResults.add(new QuizResultResponse.QuestionResult(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptions(),
                    selectedOpt,
                    q.getCorrectOptionIndex(),
                    isCorrect,
                    pointsEarned,
                    q.getPoints()
            ));
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quizId)
                .userId(student.getId())
                .answers(userAnswers)
                .score(totalScore)
                .totalMarks(quiz.getTotalMarks())
                .completed(true)
                .submittedAt(Instant.now())
                .build();

        attempt = quizAttemptRepository.save(attempt);

        double percentage = quiz.getTotalMarks() > 0
                ? Math.round((double) totalScore / quiz.getTotalMarks() * 100 * 10.0) / 10.0
                : 0.0;

        return new QuizResultResponse(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                totalScore,
                quiz.getTotalMarks(),
                percentage,
                true,
                attempt.getSubmittedAt(),
                questionResults
        );
    }

    public QuizResultResponse getResult(String quizId, User student) {
        Quiz quiz = getById(quizId);
        QuizAttempt attempt = quizAttemptRepository.findByQuizIdAndUserId(quizId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No submission found for this test."));

        Map<String, Integer> userAnswers = attempt.getAnswers() != null ? attempt.getAnswers() : Map.of();
        List<QuizResultResponse.QuestionResult> questionResults = new ArrayList<>();

        for (QuizQuestion q : quiz.getQuestions()) {
            Integer selectedOpt = userAnswers.get(q.getId());
            boolean isCorrect = selectedOpt != null && selectedOpt == q.getCorrectOptionIndex();
            int pointsEarned = isCorrect ? q.getPoints() : 0;

            questionResults.add(new QuizResultResponse.QuestionResult(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptions(),
                    selectedOpt,
                    q.getCorrectOptionIndex(),
                    isCorrect,
                    pointsEarned,
                    q.getPoints()
            ));
        }

        double percentage = quiz.getTotalMarks() > 0
                ? Math.round((double) attempt.getScore() / quiz.getTotalMarks() * 100 * 10.0) / 10.0
                : 0.0;

        return new QuizResultResponse(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                attempt.getScore(),
                quiz.getTotalMarks(),
                percentage,
                attempt.isCompleted(),
                attempt.getSubmittedAt(),
                questionResults
        );
    }

    public Quiz getById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Test / Quiz", "id", id));
    }
}
