package com.codearena.submission;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findTop20ByUserIdAndProblemIdOrderByCreatedAtDesc(String userId, String problemId);
    List<Submission> findByContestIdOrderByCreatedAtAsc(String contestId);
    List<Submission> findByVerdictOrderByCreatedAtAsc(Verdict verdict);
    long countByProblemIdAndVerdict(String problemId, Verdict verdict);
    long countByProblemId(String problemId);
    boolean existsByUserIdAndProblemIdAndVerdict(String userId, String problemId, Verdict verdict);
    List<Submission> findByContestIdIn(List<String> contestIds);
    List<Submission> findByUserId(String userId);
    List<Submission> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}
