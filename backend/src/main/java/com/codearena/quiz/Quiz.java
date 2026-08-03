package com.codearena.quiz;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends BaseEntity {

    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private int durationMinutes;
    private int totalMarks;
    private String createdById;

    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    @Transient
    public QuizStatus getStatus() {
        Instant now = Instant.now();
        if (startTime != null && now.isBefore(startTime)) return QuizStatus.UPCOMING;
        if (endTime != null && now.isAfter(endTime)) return QuizStatus.ENDED;
        return QuizStatus.LIVE;
    }
}
