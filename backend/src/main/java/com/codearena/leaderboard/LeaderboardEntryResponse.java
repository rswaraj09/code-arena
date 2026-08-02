package com.codearena.leaderboard;

public record LeaderboardEntryResponse(
        int rank,
        String userId,
        String name,
        String college,
        int solved,
        long score,
        long totalRuntimeMs
) {
}
