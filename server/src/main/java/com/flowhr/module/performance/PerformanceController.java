package com.flowhr.module.performance;

import com.flowhr.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/my/goals")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myGoals(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int year) {
        if (year == 0)
            year = Year.now().getValue();
        return ResponseEntity.ok(ApiResponse.success(performanceService.getMyGoals(user.getUsername(), year)));
    }

    @GetMapping("/employees/{employeeId}/goals")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> goalsForEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int year) {
        if (year == 0)
            year = Year.now().getValue();
        return ResponseEntity.ok(ApiResponse.success(performanceService.getGoals(employeeId, year)));
    }

    @PostMapping("/employees/{employeeId}/goals")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createGoal(
            @PathVariable Long employeeId,
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal UserDetails user) {
        Long id = performanceService.createGoal(employeeId, request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Goal berhasil dibuat"));
    }

    @PostMapping("/employees/{employeeId}/appraisals")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createAppraisal(
            @PathVariable Long employeeId,
            @RequestBody AppraisalRequest request,
            @AuthenticationPrincipal UserDetails user) {
        Long id = performanceService.createAppraisal(employeeId, request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Appraisal berhasil dibuat"));
    }

    @GetMapping("/employees/{employeeId}/appraisals")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAppraisals(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getAppraisals(employeeId)));
    }

    @PutMapping("/appraisals/{id}/submit")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> submitAppraisal(@PathVariable Long id) {
        performanceService.submitAppraisal(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Appraisal disubmit"));
    }

    @PutMapping("/appraisals/{id}/finalize")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> finalizeAppraisal(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal bonusRecommendation,
            @RequestParam(defaultValue = "false") boolean promotionRecommendation) {
        performanceService.finalizeAppraisal(id, bonusRecommendation, promotionRecommendation);
        return ResponseEntity.ok(ApiResponse.success(null, "Appraisal difinalisasi"));
    }
}
