package com.codearena.problem;

import com.codearena.common.ApiResponse;
import com.codearena.problem.dto.CreateProblemRequest;
import com.codearena.problem.dto.ProblemDetailResponse;
import com.codearena.problem.dto.ProblemSummaryResponse;
import com.codearena.problem.dto.RunRequest;
import com.codearena.problem.dto.SubmitRequest;
import com.codearena.security.UserPrincipal;
import com.codearena.submission.SubmissionService;
import com.codearena.submission.dto.RunResultResponse;
import com.codearena.submission.dto.SubmissionHistoryResponse;
import com.codearena.submission.dto.SubmitResultResponse;
import com.codearena.user.User;
import com.codearena.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "Browse problems and run/submit code against the sandboxed judge")
public class ProblemController {

    private final ProblemService problemService;
    private final SubmissionService submissionService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List published problems, optionally filtered by difficulty")
    public ResponseEntity<ApiResponse<Page<ProblemSummaryResponse>>> list(
            @RequestParam(required = false) Difficulty difficulty,
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        User currentUser = principal == null ? null : userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(problemService.list(difficulty, pageable, currentUser)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full problem detail (description, examples, constraints, hints)")
    public ResponseEntity<ApiResponse<ProblemDetailResponse>> detail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getDetail(slug)));
    }

    @PostMapping("/{slug}/run")
    @Operation(summary = "Run code against custom input or the first visible example — not graded, not persisted")
    public ResponseEntity<ApiResponse<RunResultResponse>> run(
            @PathVariable String slug,
            @Valid @RequestBody RunRequest request
    ) {
        RunResultResponse result = submissionService.run(slug, request.language(), request.code(), request.customInput());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{slug}/submit")
    @Operation(summary = "Submit for grading against all test cases; updates the leaderboard")
    public ResponseEntity<ApiResponse<SubmitResultResponse>> submit(
            @PathVariable String slug,
            @Valid @RequestBody SubmitRequest request,
            @RequestParam(required = false) String contestId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userService.getById(principal.getId());
        SubmitResultResponse result = submissionService.submit(slug, request.language(), request.code(), user, contestId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{slug}/submissions")
    @Operation(summary = "The current user's recent submissions for this problem")
    public ResponseEntity<ApiResponse<List<SubmissionHistoryResponse>>> submissions(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(submissionService.history(slug, user)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Create a new problem with its test cases (trainer/admin only)")
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody CreateProblemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User creator = userService.getById(principal.getId());
        problemService.create(request, creator);
        return ResponseEntity.ok(ApiResponse.message("Problem created. Publish it once it's reviewed."));
    }
}
