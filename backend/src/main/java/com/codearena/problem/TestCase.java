package com.codearena.problem;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseEntity {

    @DocumentReference
    private Problem problem;

    private String input;
    private String expectedOutput;

    @Builder.Default
    private boolean hidden = true;

    @Builder.Default
    private Integer weight = 1;
}
