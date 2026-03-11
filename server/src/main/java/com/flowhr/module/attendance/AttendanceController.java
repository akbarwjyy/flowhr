package com.flowhr.module.attendance;

import com.flowhr.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> clockIn(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0.0") Double lat,
            @RequestParam(defaultValue = "0.0") Double lng) {
        attendanceService.clockIn(user.getUsername(), lat, lng);
        return ResponseEntity.ok(ApiResponse.success(null, "Clock-in berhasil"));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> clockOut(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0.0") Double lat,
            @RequestParam(defaultValue = "0.0") Double lng) {
        attendanceService.clockOut(user.getUsername(), lat, lng);
        return ResponseEntity.ok(ApiResponse.success(null, "Clock-out berhasil"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyAttendance(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyAttendance(user.getUsername(), from, to)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAllAttendance(from, to, page, size)));
    }
}
