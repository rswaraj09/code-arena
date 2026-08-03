package com.codearena.contest;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ContestRepository extends MongoRepository<Contest, String> {
    List<Contest> findByCreatedById(String trainerId);
    List<Contest> findByCreatedByIdOrderByStartTimeDesc(String trainerId);
    long countByCreatedById(String trainerId);
}
