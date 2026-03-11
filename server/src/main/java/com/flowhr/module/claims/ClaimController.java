package com.flowhr.module.claims;

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
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(claimService.getCategories()));
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> submit(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ClaimSubmitRequest request) {
        Long id = claimService.submit(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Klaim berhasil diajukan"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myClaims(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(claimService.getMyClaims(user.getUsername())));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> pending(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(claimService.getPendingForManager(user.getUsername())));
    }

    @PutMapping("/{id}/approve/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> approveManager(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String notes) {
        claimService.approveByManager(id, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Klaim disetujui oleh manajer"));
    }

    @PutMapping("/{id}/approve/hr")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveHR(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String notes) {
        claimService.approveByHR(id, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Klaim disetujui oleh HR"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String notes) {
        claimService.reject(id, notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Klaim ditolak"));
    }
}
