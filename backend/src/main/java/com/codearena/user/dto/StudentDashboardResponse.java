package com.codearena.user.dto;

import java.time.Instant;
import java.util.List;

public record StudentDashboardResponse(
        String rankLabel,
        String rankPercentile,
        long solvedCount,
        long totalProblemsCount,
        int streakDays,
        int personalBestStreak,
        long pendingCount,
        List<DailyActivity> weeklyActivity,
        List<UpcomingEvent> upcomingEvents,
        List<RecentSubmissionItem> recentSubmissions,
        List<SkillProgressItem> skillProgress
) {
    public record DailyActivity(String day, int count) {}
    public record UpcomingEvent(String id, String title, String when, String type) {}
    public record RecentSubmissionItem(String id, String problemTitle, String verdict, String language, Instant createdAt) {}
    public record SkillProgressItem(String label, double percentage) {}
}
