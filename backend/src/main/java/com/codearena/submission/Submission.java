package com.codearena.submission;

import com.codearena.common.BaseEntity;
import com.codearena.problem.Problem;
import com.codearena.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission extends BaseEntity {

    @DocumentReference
    private User user;

    @DocumentReference
    private Problem problem;

    private String contestId;

    private Language language;
    private String code;

    @Builder.Default
    private Verdict verdict = Verdict.PENDING;

    private Long runtimeMs;
    private Long memoryKb;
    private Integer testCasesPassed;
    private Integer testCasesTotal;
    private String judgeOutput;
}
