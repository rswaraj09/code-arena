package com.codearena.problem;

import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.problem.dto.*;
import com.codearena.submission.Submission;
import com.codearena.submission.SubmissionRepository;
import com.codearena.submission.Verdict;
import com.codearena.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;

    public Page<ProblemSummaryResponse> list(Difficulty difficulty, Pageable pageable, User currentUser) {
        Page<Problem> page = difficulty != null
                ? problemRepository.findByPublishedTrueAndDifficulty(difficulty, pageable)
                : problemRepository.findByPublishedTrue(pageable);

        return page.map(problem -> toSummary(problem, currentUser));
    }

    public ProblemDetailResponse getDetail(String slug) {
        Problem problem = getPublishedBySlug(slug);
        List<ExampleResponse> examples = testCaseRepository.findByProblemIdAndHiddenFalse(problem.getId()).stream()
                .map(tc -> new ExampleResponse(tc.getInput(), tc.getExpectedOutput()))
                .toList();

        return new ProblemDetailResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDifficulty(),
                problem.getDescription(),
                problem.getConstraints(),
                problem.getTags(),
                problem.getHints(),
                examples,
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb()
        );
    }

    public Problem getPublishedBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Problem", "slug", slug));
        if (!problem.isPublished()) {
            throw ResourceNotFoundException.of("Problem", "slug", slug);
        }
        return problem;
    }

    @Transactional
    public Problem create(CreateProblemRequest request, User creator) {
        String slug = slugify(request.title());
        if (problemRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("A problem with a matching title/slug already exists.");
        }

        Problem problem = Problem.builder()
                .title(request.title())
                .slug(slug)
                .difficulty(request.difficulty())
                .description(request.description())
                .constraints(request.constraints())
                .tags(request.tags() == null ? List.of() : request.tags())
                .hints(request.hints() == null ? List.of() : request.hints())
                .timeLimitMs(request.timeLimitMs() == null ? 1000 : request.timeLimitMs())
                .memoryLimitMb(request.memoryLimitMb() == null ? 256 : request.memoryLimitMb())
                .createdById(creator.getId())
                .published(false) // requires explicit publish step, e.g. after admin review
                .build();

        List<TestCase> testCases = request.testCases().stream()
                .map(tc -> TestCase.builder()
                        .problem(problem)
                        .input(tc.input())
                        .expectedOutput(tc.expectedOutput())
                        .hidden(tc.hidden())
                        .weight(tc.weight() == null ? 1 : tc.weight())
                        .build())
                .collect(Collectors.toList());
        problem.setTestCases(testCases);

        return problemRepository.save(problem);
    }

    private ProblemSummaryResponse toSummary(Problem problem, User currentUser) {
        long accepted = submissionRepository.countByProblemIdAndVerdict(problem.getId(), Verdict.ACCEPTED);
        long total = submissionRepository.countByProblemId(problem.getId());
        double acceptanceRate = total == 0 ? 0.0 : Math.round((accepted * 1000.0 / total)) / 10.0;

        boolean solved = currentUser != null
                && submissionRepository.existsByUserIdAndProblemIdAndVerdict(currentUser.getId(), problem.getId(), Verdict.ACCEPTED);

        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDifficulty(),
                problem.getTags(),
                solved,
                acceptanceRate
        );
    }

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private String slugify(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
        String slug = NON_ALPHANUMERIC.matcher(normalized).replaceAll("-").replaceAll("^-|-$", "");
        return slug;
    }
}
