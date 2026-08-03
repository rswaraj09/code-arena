package com.codearena.user;

import com.codearena.contest.Contest;
import com.codearena.contest.ContestRepository;
import com.codearena.leaderboard.LeaderboardEntryResponse;
import com.codearena.leaderboard.LeaderboardService;
import com.codearena.problem.Problem;
import com.codearena.problem.ProblemRepository;
import com.codearena.submission.Submission;
import com.codearena.submission.SubmissionRepository;
import com.codearena.submission.Verdict;
import com.codearena.user.dto.StudentDashboardResponse;
import com.codearena.user.dto.StudentListItemResponse;
import com.codearena.user.dto.StudentDashboardResponse.DailyActivity;
import com.codearena.user.dto.StudentDashboardResponse.RecentSubmissionItem;
import com.codearena.user.dto.StudentDashboardResponse.SkillProgressItem;
import com.codearena.user.dto.StudentDashboardResponse.UpcomingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final ContestRepository contestRepository;
    private final StudentRepository studentRepository;
    private final LeaderboardService leaderboardService;

    public StudentDashboardResponse getDashboard(String userId) {
        // 1. All user submissions
        List<Submission> userSubmissions = submissionRepository.findByUserId(userId);
        List<Submission> recentSubmissionsList = submissionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);

        // 2. Solved problems count with safe reference resolution
        Set<String> solvedProblemIds = userSubmissions.stream()
                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                .map(s -> {
                    try {
                        return s.getProblem() != null ? s.getProblem().getId() : null;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long solvedCount = solvedProblemIds.size();
        long totalProblemsCount = problemRepository.count();

        // 3. Rank & Percentile
        String rankLabel = "Unranked";
        String rankPercentile = "Solve problems to rank";
        List<LeaderboardEntryResponse> standings = leaderboardService.getLeaderboard(null);
        long totalStudents = Math.max(1, studentRepository.count());

        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i).userId().equals(userId)) {
                int rank = i + 1;
                rankLabel = "#" + rank;
                long pct = Math.max(1, Math.round(((double) rank / totalStudents) * 100));
                rankPercentile = "Top " + pct + "% overall";
                break;
            }
        }

        // 4. Streak Calculation
        Set<LocalDate> activeDates = userSubmissions.stream()
                .map(s -> s.getCreatedAt() != null ? s.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        int streakDays = 0;
        int personalBestStreak = 0;
        if (!activeDates.isEmpty()) {
            List<LocalDate> sortedDates = new ArrayList<>(activeDates);
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            LocalDate yesterday = today.minusDays(1);

            // Compute current streak
            LocalDate checkDate = activeDates.contains(today) ? today : (activeDates.contains(yesterday) ? yesterday : null);
            if (checkDate != null) {
                while (activeDates.contains(checkDate)) {
                    streakDays++;
                    checkDate = checkDate.minusDays(1);
                }
            }

            // Compute max streak
            int tempStreak = 1;
            personalBestStreak = 1;
            for (int i = 1; i < sortedDates.size(); i++) {
                if (sortedDates.get(i).equals(sortedDates.get(i - 1).plusDays(1))) {
                    tempStreak++;
                } else {
                    tempStreak = 1;
                }
                personalBestStreak = Math.max(personalBestStreak, tempStreak);
            }
            personalBestStreak = Math.max(personalBestStreak, streakDays);
        }

        // 5. Weekly activity (Mon - Sun of current week)
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime monday = nowUtc.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay(ZoneOffset.UTC);

        Map<DayOfWeek, Integer> dailyCount = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            dailyCount.put(day, 0);
        }

        for (Submission s : userSubmissions) {
            if (s.getCreatedAt() != null) {
                ZonedDateTime dt = s.getCreatedAt().atZone(ZoneOffset.UTC);
                if (!dt.isBefore(monday) && dt.isBefore(monday.plusWeeks(1))) {
                    dailyCount.put(dt.getDayOfWeek(), dailyCount.get(dt.getDayOfWeek()) + 1);
                }
            }
        }

        List<DailyActivity> weeklyActivity = List.of(
                new DailyActivity("Mon", dailyCount.get(DayOfWeek.MONDAY)),
                new DailyActivity("Tue", dailyCount.get(DayOfWeek.TUESDAY)),
                new DailyActivity("Wed", dailyCount.get(DayOfWeek.WEDNESDAY)),
                new DailyActivity("Thu", dailyCount.get(DayOfWeek.THURSDAY)),
                new DailyActivity("Fri", dailyCount.get(DayOfWeek.FRIDAY)),
                new DailyActivity("Sat", dailyCount.get(DayOfWeek.SATURDAY)),
                new DailyActivity("Sun", dailyCount.get(DayOfWeek.SUNDAY))
        );

        // 6. Upcoming Events (Live or Upcoming Contests)
        Instant now = Instant.now();
        List<Contest> upcomingContests = contestRepository.findAll().stream()
                .filter(c -> c.getEndTime() != null && c.getEndTime().isAfter(now))
                .sorted(Comparator.comparing(Contest::getStartTime))
                .limit(4)
                .toList();

        long pendingCount = upcomingContests.size();

        List<UpcomingEvent> upcomingEvents = upcomingContests.stream()
                .map(c -> {
                    String when;
                    if (c.getStartTime().isBefore(now)) {
                        when = "Live now";
                    } else {
                        long hours = Duration.between(now, c.getStartTime()).toHours();
                        if (hours < 24) {
                            when = hours <= 1 ? "In less than an hour" : "In " + hours + " hours";
                        } else {
                            long days = hours / 24;
                            when = "In " + days + " day" + (days > 1 ? "s" : "");
                        }
                    }
                    return new UpcomingEvent(c.getId(), c.getTitle(), when, "Contest");
                })
                .toList();

        // 7. Recent Submissions List with safe title lookup
        List<RecentSubmissionItem> recentItems = recentSubmissionsList.stream()
                .map(s -> {
                    String problemTitle = "Problem";
                    try {
                        if (s.getProblem() != null && s.getProblem().getTitle() != null) {
                            problemTitle = s.getProblem().getTitle();
                        }
                    } catch (Exception e) {
                        // ignore broken reference
                    }
                    return new RecentSubmissionItem(
                            s.getId(),
                            problemTitle,
                            s.getVerdict() != null ? s.getVerdict().name() : "PENDING",
                            s.getLanguage() != null ? s.getLanguage().name() : "Code",
                            s.getCreatedAt()
                    );
                })
                .toList();

        // 8. Skill Progress by Tags
        List<Problem> allProblems = problemRepository.findAll();
        Map<String, List<Problem>> tagToProblems = new HashMap<>();

        for (Problem p : allProblems) {
            if (p.getTags() != null && !p.getTags().isEmpty()) {
                for (String tag : p.getTags()) {
                    tagToProblems.computeIfAbsent(tag.trim(), k -> new ArrayList<>()).add(p);
                }
            }
        }

        // Default categories if system tags are sparse
        if (tagToProblems.isEmpty()) {
            tagToProblems.put("Arrays & Strings", allProblems);
            tagToProblems.put("Dynamic Programming", allProblems);
            tagToProblems.put("Graphs", allProblems);
            tagToProblems.put("Algorithms", allProblems);
        }

        List<SkillProgressItem> skillProgress = tagToProblems.entrySet().stream()
                .limit(4)
                .map(entry -> {
                    String tag = entry.getKey();
                    List<Problem> problemsWithTag = entry.getValue();
                    long solvedWithTag = problemsWithTag.stream()
                            .filter(p -> solvedProblemIds.contains(p.getId()))
                            .count();
                    double pct = problemsWithTag.isEmpty() ? 0 : Math.round((double) solvedWithTag / problemsWithTag.size() * 100 * 10.0) / 10.0;
                    return new SkillProgressItem(tag, pct);
                })
                .toList();

        return new StudentDashboardResponse(
                rankLabel,
                rankPercentile,
                solvedCount,
                totalProblemsCount,
                streakDays,
                personalBestStreak,
                pendingCount,
                weeklyActivity,
                upcomingEvents,
                recentItems,
                skillProgress
        );
    }

    public List<StudentListItemResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> {
                    long solvedCount = 0;
                    long totalSubmissions = 0;
                    try {
                        List<Submission> submissions = submissionRepository.findByUserId(student.getId());
                        totalSubmissions = submissions.size();
                        solvedCount = submissions.stream()
                                .filter(s -> s.getVerdict() == Verdict.ACCEPTED)
                                .map(s -> {
                                    try {
                                        return s.getProblem() != null ? s.getProblem().getId() : null;
                                    } catch (Exception e) {
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .distinct()
                                .count();
                    } catch (Exception e) {
                        log.warn("Error computing metrics for student {}", student.getId(), e);
                    }
                    return new StudentListItemResponse(
                            student.getId(),
                            student.getName() != null ? student.getName() : "Student",
                            student.getEmail(),
                            student.getCollege(),
                            student.getYear(),
                            student.getBranch(),
                            student.isEmailVerified(),
                            solvedCount,
                            totalSubmissions,
                            student.getCreatedAt()
                    );
                })
                .sorted(Comparator.comparing(StudentListItemResponse::solvedCount).reversed())
                .toList();
    }
}
