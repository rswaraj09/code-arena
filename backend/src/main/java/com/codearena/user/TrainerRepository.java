package com.codearena.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrainerRepository extends MongoRepository<Trainer, String> {
    Optional<Trainer> findByEmail(String email);
    boolean existsByEmail(String email);
}
