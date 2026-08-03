package com.codearena.quiz;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
    Optional<QuizAttempt> findByQuizIdAndUserId(String quizId, String userId);
    List<QuizAttempt> findByUserId(String userId);
    boolean existsByQuizIdAndUserId(String quizId, String userId);
}
