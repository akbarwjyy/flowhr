package com.flowhr.module.payroll;

import com.flowhr.common.response.ApiResponse;
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
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBatches() {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getBatches()));
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createBatch(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long departmentId) {
        Long id = payrollService.createBatch(year, month, departmentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Payroll batch berhasil dibuat"));
    }

    @PostMapping("/batches/{id}/process")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> processBatch(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        payrollService.processBatch(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Payroll berhasil diproses"));
    }

    @GetMapping("/batches/{id}/payslips")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPayslips(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayslipsForBatch(id)));
    }

    @GetMapping("/my-payslips")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myPayslips(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getMyPayslips(user.getUsername()),
                "Slip gaji berhasil diambil"));
    }
}
