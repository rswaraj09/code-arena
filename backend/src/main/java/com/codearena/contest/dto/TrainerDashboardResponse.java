package com.codearena.contest.dto;

import java.util.List;

public record TrainerDashboardResponse(
        long studentCount,
        long activeContestCount,
        String activeContestSublabel,
        long myContestCount,
        long pendingSubmissions,
        double avgScorePercent,
        List<EventScore> eventScores,
        List<TopPerformer> topPerformers,
        List<ContestParticipation> contestParticipation
) {
    public record EventScore(String label, double avgScore) {}
    public record TopPerformer(String name, long score, int solved) {}
    public record ContestParticipation(String label, double participationPercent) {}
}
