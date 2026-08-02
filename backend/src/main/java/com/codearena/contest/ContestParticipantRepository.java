package com.codearena.contest;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContestParticipantRepository extends MongoRepository<ContestParticipant, String> {
    boolean existsByContestIdAndUserId(String contestId, String userId);
    List<ContestParticipant> findByContestId(String contestId);
}
