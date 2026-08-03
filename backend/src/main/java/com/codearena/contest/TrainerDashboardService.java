package com.codearena.contest;

import com.codearena.contest.dto.TrainerDashboardResponse;
import com.codearena.contest.dto.TrainerDashboardResponse.ContestParticipation;
import com.codearena.contest.dto.TrainerDashboardResponse.EventScore;
import com.codearena.contest.dto.TrainerDashboardResponse.TopPerformer;
import com.codearena.submission.Submission;
import com.codearena.submission.SubmissionRepository;
import com.codearena.submission.Verdict;
import com.codearena.user.StudentRepository;
import com.codearena.user.User;
import com.codearena.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerDashboardService {

    private final ContestRepository contestRepository;
    private final ContestParticipantRepository participantRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final UserService userService;

    public TrainerDashboardResponse getDashboard(String trainerId) {

        // ---- 1. Student count (global) ----
        long studentCount = studentRepository.count();

        // ---- 2. Trainer's contests ----
        List<Contest> myContests = contestRepository.findByCreatedByIdOrderByStartTimeDesc(trainerId);
        long myContestCount = myContests.size();

        // Active = LIVE + UPCOMING
        long activeContestCount = myContests.stream()
                .filter(c -> c.getStatus() == ContestStatus.LIVE || c.getStatus() == ContestStatus.UPCOMING)
                .count();

        // Build sublabel like "1 starting today" or "2 live now"
        ZonedDateTime todayStart = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime todayEnd = todayStart.plusDays(1);
        long startingToday = myContests.stream()
                .filter(c -> c.getStartTime() != null
                        && !c.getStartTime().isBefore(todayStart.toInstant())
                        && c.getStartTime().isBefore(todayEnd.toInstant()))
                .count();
        long liveNow = myContests.stream().filter(c -> c.getStatus() == ContestStatus.LIVE).count();

        String activeContestSublabel;
        if (liveNow > 0) {
            activeContestSublabel = liveNow + " live now";
        } else if (startingToday > 0) {
            activeContestSublabel = startingToday + " starting today";
        } else {
            activeContestSublabel = activeContestCount > 0 ? activeContestCount + " upcoming" : "None scheduled";
        }

        // ---- 3. Submissions for this trainer's contests ----
        List<String> contestIds = myContests.stream().map(Contest::getId).toList();
        List<Submission> allSubmissions = contestIds.isEmpty()
                ? List.of()
                : submissionRepository.findByContestIdIn(contestIds);

        long pendingSubmissions = allSubmissions.stream()
                .filter(s -> s.getVerdict() == Verdict.PENDING)
                .count();

        // ---- 4. Average score across all trainer's contests ----
        double avgScorePercent = 0.0;
        List<Submission> gradedSubmissions = allSubmissions.stream()
                .filter(s -> s.getVerdict() != Verdict.PENDING && s.getTestCasesTotal() != null && s.getTestCasesTotal() > 0)
                .toList();
        if (!gradedSubmissions.isEmpty()) {
            double totalPercent = gradedSubmissions.stream()
                    .mapToDouble(s -> (double) s.getTestCasesPassed() / s.getTestCasesTotal() * 100)
                    .sum();
            avgScorePercent = Math.round(totalPercent / gradedSubmissions.size() * 10.0) / 10.0;
        }

        // ---- 5. Average score by event (bar chart) — last 6 contests ----
        List<Contest> recentContests = myContests.stream().limit(6).toList();
        Map<String, List<Submission>> submissionsByContest = allSubmissions.stream()
                .filter(s -> s.getContestId() != null)
                .collect(Collectors.groupingBy(Submission::getContestId));

        List<EventScore> eventScores = new ArrayList<>();
        for (Contest contest : recentContests) {
            List<Submission> contestSubs = submissionsByContest.getOrDefault(contest.getId(), List.of())
                    .stream()
                    .filter(s -> s.getVerdict() != Verdict.PENDING && s.getTestCasesTotal() != null && s.getTestCasesTotal() > 0)
                    .toList();
            double avg = contestSubs.isEmpty() ? 0 : contestSubs.stream()
                    .mapToDouble(s -> (double) s.getTestCasesPassed() / s.getTestCasesTotal() * 100)
                    .average()
                    .orElse(0);
            eventScores.add(new EventScore(contest.getTitle(), Math.round(avg * 10.0) / 10.0));
        }
        // Reverse so oldest first (chronological left-to-right on chart)
        Collections.reverse(eventScores);

        // ---- 6. Top performers (across this trainer's contests) ----
        List<Submission> accepted = allSubmissions.stream()
                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                .toList();

        Map<String, List<Submission>> byUserId = accepted.stream()
                .collect(Collectors.groupingBy(Submission::getUserId));

        List<TopPerformer> topPerformers = byUserId.entrySet().stream()
                .map(entry -> {
                    int solved = (int) entry.getValue().stream()
                            .map(s -> s.getProblem().getId())
                            .distinct()
                            .count();
                    long score = solved * 100L;
                    String name;
                    try {
                        User user = userService.getById(entry.getKey());
                        name = user.getName();
                    } catch (Exception e) {
                        name = "Unknown";
                    }
                    return new TopPerformer(name, score, solved);
                })
                .sorted(Comparator.comparingLong(TopPerformer::score).reversed()
                        .thenComparingInt(TopPerformer::solved).reversed())
                .limit(5)
                .toList();

        // ---- 7. Contest participation rates (replaces workshop attendance) ----
        List<ContestParticipation> contestParticipation = new ArrayList<>();
        for (Contest contest : recentContests.stream().limit(5).toList()) {
            long participants = participantRepository.findByContestId(contest.getId()).size();
            double rate = studentCount > 0 ? Math.round((double) participants / studentCount * 100 * 10.0) / 10.0 : 0;
            contestParticipation.add(new ContestParticipation(contest.getTitle(), rate));
        }

        return new TrainerDashboardResponse(
                studentCount,
                activeContestCount,
                activeContestSublabel,
                myContestCount,
                pendingSubmissions,
                avgScorePercent,
                eventScores,
                topPerformers,
                contestParticipation
        );
    }
}
