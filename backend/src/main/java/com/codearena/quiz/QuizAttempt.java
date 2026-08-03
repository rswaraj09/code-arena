package com.codearena.quiz;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "quiz_attempts")
@CompoundIndex(name = "quiz_user_attempt_idx", def = "{'quizId': 1, 'userId': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt extends BaseEntity {

    private String quizId;
    private String userId;

    @Builder.Default
    private Map<String, Integer> answers = new HashMap<>();

    private int score;
    private int totalMarks;
    private boolean completed;
    private Instant submittedAt;
}
