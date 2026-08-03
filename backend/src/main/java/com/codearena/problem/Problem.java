package com.codearena.problem;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem extends BaseEntity {

    private String title;

    @Indexed(unique = true)
    private String slug;

    private Difficulty difficulty;
    private String description;
    private String constraints;
    private String editorial;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<String> hints = new ArrayList<>();

    @Builder.Default
    private Integer timeLimitMs = 1000;

    @Builder.Default
    private Integer memoryLimitMb = 256;

    @Builder.Default
    private boolean published = false;

    private String createdById;

    @DocumentReference
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();
}
