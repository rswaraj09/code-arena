package com.codearena.quiz;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByCreatedById(String trainerId);
    List<Quiz> findAllByOrderByCreatedAtDesc();
}
