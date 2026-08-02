package com.codearena.problem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProblemRepository extends MongoRepository<Problem, String> {
    Optional<Problem> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Problem> findByPublishedTrueAndDifficulty(Difficulty difficulty, Pageable pageable);
    Page<Problem> findByPublishedTrue(Pageable pageable);
}
