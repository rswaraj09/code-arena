package com.codearena.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {
    private String id;
    private String questionText;

    @Builder.Default
    private List<String> options = new ArrayList<>();

    private int correctOptionIndex;

    @Builder.Default
    private int points = 5;
}
