package com.codearena.submission;

import com.codearena.leaderboard.LeaderboardService;
import com.codearena.problem.Problem;
import com.codearena.problem.ProblemService;
import com.codearena.problem.TestCase;
import com.codearena.problem.TestCaseRepository;
import com.codearena.submission.dto.RunResultResponse;
import com.codearena.submission.dto.SubmissionHistoryResponse;
import com.codearena.submission.dto.SubmitResultResponse;
import com.codearena.submission.judge.JudgeResult;
import com.codearena.submission.judge.JudgeService;
import com.codearena.submission.judge.OutputComparator;
import com.codearena.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final ProblemService problemService;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;
    private final LeaderboardService leaderboardService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * "Run" — executes against either the student's custom input or the
     * problem's first visible example. Nothing is persisted; this is a
     * scratchpad action, not a graded attempt.
     */
    public RunResultResponse run(String slug, Language language, String code, String customInput) {
        Problem problem = problemService.getPublishedBySlug(slug);

        String stdin = customInput;
        if (stdin == null || stdin.isBlank()) {
            List<TestCase> visible = testCaseRepository.findByProblemIdAndHiddenFalse(problem.getId());
            stdin = visible.isEmpty() ? "" : visible.get(0).getInput();
        }

        JudgeResult result = judgeService.execute(language, code, stdin, problem.getTimeLimitMs(), problem.getMemoryLimitMb());
        Verdict verdict = result.verdict() == Verdict.PENDING ? Verdict.ACCEPTED : result.verdict();
        String output = result.verdict() == Verdict.PENDING ? result.stdout() : result.stderr();

        return new RunResultResponse(verdict, output, result.stderr(), result.runtimeMs());
    }

    /**
     * "Submit" — runs every hidden + visible test case, persists the result,
     * and broadcasts the updated leaderboard over WebSocket so every
     * connected client sees the new standing without polling.
     */
    public SubmitResultResponse submit(String slug, Language language, String code, User user, String contestId) {
        Problem problem = problemService.getPublishedBySlug(slug);
        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());

        Verdict finalVerdict = Verdict.ACCEPTED;
        int passed = 0;
        long maxRuntimeMs = 0;
        String failingOutput = null;

        for (TestCase testCase : testCases) {
            JudgeResult result = judgeService.execute(language, code, testCase.getInput(), problem.getTimeLimitMs(), problem.getMemoryLimitMb());
            maxRuntimeMs = Math.max(maxRuntimeMs, result.runtimeMs());

            Verdict caseVerdict = result.verdict() == Verdict.PENDING
                    ? OutputComparator.compare(result.stdout(), testCase.getExpectedOutput())
                    : result.verdict();

            if (caseVerdict == Verdict.ACCEPTED) {
                passed++;
            } else if (finalVerdict == Verdict.ACCEPTED) {
                // Keep the first failure encountered as the submission's verdict.
                finalVerdict = caseVerdict;
                failingOutput = result.verdict() == Verdict.PENDING ? result.stdout() : result.stderr();
            }
        }

        Submission submission = Submission.builder()
                .userId(user.getId())
                .problem(problem)
                .contestId(contestId)
                .language(language)
                .code(code)
                .verdict(finalVerdict)
                .runtimeMs(maxRuntimeMs)
                .testCasesPassed(passed)
                .testCasesTotal(testCases.size())
                .judgeOutput(failingOutput)
                .build();
        submissionRepository.save(submission);

        broadcastLeaderboardUpdate(contestId);

        return new SubmitResultResponse(submission.getId(), finalVerdict, passed, testCases.size(), maxRuntimeMs, failingOutput);
    }

    public List<SubmissionHistoryResponse> history(String slug, User user) {
        Problem problem = problemService.getPublishedBySlug(slug);
        return submissionRepository.findTop20ByUserIdAndProblemIdOrderByCreatedAtDesc(user.getId(), problem.getId())
                .stream()
                .map(s -> new SubmissionHistoryResponse(s.getId(), s.getLanguage(), s.getVerdict(), s.getRuntimeMs(), s.getCreatedAt()))
                .toList();
    }

    private void broadcastLeaderboardUpdate(String contestId) {
        String destination = contestId != null ? "/topic/leaderboard/" + contestId : "/topic/leaderboard/global";
        try {
            messagingTemplate.convertAndSend(destination, leaderboardService.getLeaderboard(contestId));
        } catch (Exception e) {
            log.warn("Failed to broadcast leaderboard update to {}: {}", destination, e.getMessage());
        }
    }
}
