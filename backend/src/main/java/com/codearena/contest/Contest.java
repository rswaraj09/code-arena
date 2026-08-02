package com.codearena.contest;

import com.codearena.common.BaseEntity;
import com.codearena.problem.Problem;
import com.codearena.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "contests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contest extends BaseEntity {

    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;

    @Builder.Default
    private boolean negativeMarking = false;

    @DocumentReference
    private User createdBy;

    @DocumentReference
    @Builder.Default
    private List<Problem> problems = new ArrayList<>();

    @Transient
    public ContestStatus getStatus() {
        Instant now = Instant.now();
        if (now.isBefore(startTime)) return ContestStatus.UPCOMING;
        if (now.isAfter(endTime)) return ContestStatus.ENDED;
        return ContestStatus.LIVE;
    }
}
