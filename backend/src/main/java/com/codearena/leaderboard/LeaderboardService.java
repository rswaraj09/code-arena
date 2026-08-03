package com.codearena.leaderboard;

import com.codearena.submission.Submission;
import com.codearena.submission.SubmissionRepository;
import com.codearena.submission.Verdict;
import com.codearena.user.User;
import com.codearena.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes standings on demand from accepted submissions rather than
 * maintaining a separately-updated leaderboard table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    public List<LeaderboardEntryResponse> getLeaderboard(String contestId) {
        List<Submission> accepted = (contestId != null
                ? submissionRepository.findByContestIdOrderByCreatedAtAsc(contestId)
                : submissionRepository.findByVerdictOrderByCreatedAtAsc(Verdict.ACCEPTED))
                .stream()
                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                .toList();

        Map<String, List<Submission>> byUserId = accepted.stream()
                .collect(Collectors.groupingBy(Submission::getUserId));

        record Standing(String userId, long solved, long score, long totalRuntimeMs) {
        }

        List<Standing> standings = byUserId.entrySet().stream()
                .map(entry -> {
                    long solved = entry.getValue().stream().map(s -> s.getProblem().getId()).distinct().count();
                    long totalRuntime = entry.getValue().stream().mapToLong(s -> s.getRuntimeMs() == null ? 0 : s.getRuntimeMs()).sum();
                    return new Standing(entry.getKey(), solved, solved * 100, totalRuntime);
                })
                .sorted(Comparator.comparingLong(Standing::score).reversed()
                        .thenComparingLong(Standing::totalRuntimeMs))
                .toList();

        return java.util.stream.IntStream.range(0, standings.size())
                .mapToObj(i -> {
                    Standing s = standings.get(i);
                    User user;
                    try {
                        user = userService.getById(s.userId());
                    } catch (Exception e) {
                        log.warn("Could not find user {} for leaderboard entry", s.userId());
                        return null;
                    }
                    return new LeaderboardEntryResponse(
                            i + 1,
                            user.getId(),
                            user.getName(),
                            user.getCollege(),
                            (int) s.solved(),
                            s.score(),
                            s.totalRuntimeMs()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
