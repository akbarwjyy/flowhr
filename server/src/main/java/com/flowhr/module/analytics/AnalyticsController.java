package com.flowhr.module.analytics;

import com.flowhr.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboard()));
    }

    @GetMapping("/headcount/by-department")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> headcountByDepartment() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getHeadcountByDepartment()));
    }

    @GetMapping("/turnover")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> turnover(
            @RequestParam(defaultValue = "0") int year) {
        if (year == 0)
            year = LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTurnoverRate(year)));
    }

    @GetMapping("/attendance-report")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> attendanceReport(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0)
            year = LocalDate.now().getYear();
        if (month == 0)
            month = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAttendanceReport(year, month)));
    }

    @GetMapping("/payroll-report")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> payrollReport(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0)
            year = LocalDate.now().getYear();
        if (month == 0)
            month = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPayrollReport(year, month)));
    }

    @GetMapping("/payroll-summary")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> payrollSummary(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0)
            year = LocalDate.now().getYear();
        if (month == 0)
            month = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPayrollSummary(year, month)));
    }
}
