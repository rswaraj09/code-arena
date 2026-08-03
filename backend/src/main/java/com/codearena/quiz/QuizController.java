package com.codearena.quiz;

import com.codearena.common.ApiResponse;
import com.codearena.quiz.dto.*;
import com.codearena.security.UserPrincipal;
import com.codearena.user.User;
import com.codearena.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes & Tests", description = "Trainer created tests, questions, timestamps, student attempts, and auto-grading")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all tests (upcoming, live, ended)")
    public ResponseEntity<ApiResponse<List<QuizSummaryResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = principal == null ? null : userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(quizService.list(currentUser)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test detail including questions")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getDetail(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = principal == null ? null : userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(quizService.getDetail(id, currentUser)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Create a new test with questions, options, answer keys, and timestamps (Trainer/Admin)")
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody CreateQuizRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User creator = userService.getById(principal.getId());
        quizService.create(request, creator);
        return ResponseEntity.ok(ApiResponse.message("Test created successfully."));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit test attempt for auto-grading (Student)")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submit(
            @PathVariable String id,
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User student = userService.getById(principal.getId());
        QuizResultResponse result = quizService.submit(id, request, student);
        return ResponseEntity.ok(ApiResponse.ok(result, "Test submitted and graded."));
    }

    @GetMapping("/{id}/result")
    @Operation(summary = "Get current student's test submission result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getResult(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User student = userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(quizService.getResult(id, student)));
    }
}
