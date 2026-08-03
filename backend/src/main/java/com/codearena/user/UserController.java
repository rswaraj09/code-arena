package com.codearena.user;

import com.codearena.common.ApiResponse;
import com.codearena.security.UserPrincipal;
import com.codearena.user.dto.StudentDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profiles and dashboard metrics")
public class UserController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping("/student-dashboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Aggregated dashboard stats for the current student")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> studentDashboard(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(studentDashboardService.getDashboard(principal.getId())));
    }
}
