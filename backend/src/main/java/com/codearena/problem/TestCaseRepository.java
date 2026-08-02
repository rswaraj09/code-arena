package com.codearena.problem;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestCaseRepository extends MongoRepository<TestCase, String> {
    List<TestCase> findByProblemIdAndHiddenFalse(String problemId);
    List<TestCase> findByProblemId(String problemId);
}
