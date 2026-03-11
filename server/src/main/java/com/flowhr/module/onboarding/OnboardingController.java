package com.flowhr.module.onboarding;

import com.flowhr.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> start(
            @RequestParam Long employeeId,
            @RequestParam String eventType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        Long id = onboardingService.startOnboarding(employeeId, eventType, startDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("processId", id), "Proses " + eventType + " dimulai"));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.getProcesses(employeeId)));
    }

    @GetMapping("/process/{processId}/tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTasks(
            @PathVariable Long processId) {
        return ResponseEntity.ok(ApiResponse.success(onboardingService.getTasksByProcess(processId)));
    }

    @PutMapping("/tasks/{taskId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateTask(
            @PathVariable Long taskId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        onboardingService.updateTaskStatus(taskId, status, notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Status tugas diperbarui"));
    }
}
