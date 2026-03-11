package com.flowhr.module.attendance;

import com.flowhr.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> apply(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody LeaveRequest request) {
        Long id = leaveService.applyLeave(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Pengajuan cuti berhasil dikirim"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myLeaves(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getMyLeaves(user.getUsername())));
    }

    @GetMapping("/my/quota")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> myQuota(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "ANNUAL") String type) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getQuota(user.getUsername(), type)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> pending(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getPendingForManager(user.getUsername())));
    }

    @PutMapping("/{id}/approve/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> approveManager(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String notes) {
        leaveService.approveByManager(id, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Cuti disetujui oleh manajer"));
    }

    @PutMapping("/{id}/approve/hr")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveHR(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String notes) {
        leaveService.approveByHR(id, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Cuti disetujui oleh HR"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String notes) {
        leaveService.reject(id, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Pengajuan cuti ditolak"));
    }
}
