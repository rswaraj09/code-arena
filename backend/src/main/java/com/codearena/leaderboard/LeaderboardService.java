package com.codearena.leaderboard;

import com.codearena.submission.Submission;
import com.codearena.submission.SubmissionRepository;
import com.codearena.submission.Verdict;
import com.codearena.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes standings on demand from accepted submissions rather than
 * maintaining a separately-updated leaderboard table.
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final SubmissionRepository submissionRepository;

    public List<LeaderboardEntryResponse> getLeaderboard(String contestId) {
        List<Submission> accepted = (contestId != null
                ? submissionRepository.findByContestIdOrderByCreatedAtAsc(contestId)
                : submissionRepository.findByVerdictOrderByCreatedAtAsc(Verdict.ACCEPTED))
                .stream()
                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                .toList();

        Map<User, List<Submission>> byUser = accepted.stream()
                .collect(Collectors.groupingBy(Submission::getUser));

        record Standing(User user, long solved, long score, long totalRuntimeMs) {
        }

        List<Standing> standings = byUser.entrySet().stream()
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
                    return new LeaderboardEntryResponse(
                            i + 1,
                            s.user().getId(),
                            s.user().getName(),
                            s.user().getCollege(),
                            (int) s.solved(),
                            s.score(),
                            s.totalRuntimeMs()
                    );
                })
                .toList();
    }
}
