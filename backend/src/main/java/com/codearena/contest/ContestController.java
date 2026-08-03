package com.codearena.contest;

import com.codearena.common.ApiResponse;
import com.codearena.contest.dto.ContestDetailResponse;
import com.codearena.contest.dto.ContestSummaryResponse;
import com.codearena.contest.dto.CreateContestRequest;
import com.codearena.contest.dto.TrainerDashboardResponse;
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
@RequestMapping("/api/contests")
@RequiredArgsConstructor
@Tag(name = "Contests", description = "Timed contests bundling multiple problems, with registration and a scoped leaderboard")
public class ContestController {

    private final ContestService contestService;
    private final TrainerDashboardService trainerDashboardService;
    private final UserService userService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Aggregated dashboard stats for the current trainer")
    public ResponseEntity<ApiResponse<TrainerDashboardResponse>> dashboard(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(trainerDashboardService.getDashboard(principal.getId())));
    }

    @GetMapping
    @Operation(summary = "List all contests (upcoming, live, ended)")
    public ResponseEntity<ApiResponse<List<ContestSummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(contestService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contest detail, including its problem set")
    public ResponseEntity<ApiResponse<ContestDetailResponse>> detail(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = principal == null ? null : userService.getById(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(contestService.getDetail(id, currentUser)));
    }

    @PostMapping("/{id}/register")
    @Operation(summary = "Register the current student for a contest")
    public ResponseEntity<ApiResponse<Void>> register(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        contestService.register(id, userService.getById(principal.getId()));
        return ResponseEntity.ok(ApiResponse.message("Registered for the contest."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Create a contest from an existing set of problems (trainer/admin only)")
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody CreateContestRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        contestService.create(request, userService.getById(principal.getId()));
        return ResponseEntity.ok(ApiResponse.message("Contest created."));
    }
}
