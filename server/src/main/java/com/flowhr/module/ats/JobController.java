package com.flowhr.module.ats;

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

/**
 * Job Controller — publik endpoint untuk career page (/api/v1/careers/**) +
 * internal ATS
 */
@RestController
@RequiredArgsConstructor
public class JobController {

    private final AtsService atsService;

    // ─── Public endpoints (career page) ──────────────────────────────────────
    @GetMapping("/api/v1/careers/jobs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOpenJobs() {
        return ResponseEntity.ok(ApiResponse.success(atsService.getOpenJobs()));
    }

    @PostMapping("/api/v1/careers/jobs/{jobId}/apply")
    public ResponseEntity<ApiResponse<Map<String, Long>>> apply(
            @PathVariable Long jobId,
            @Valid @RequestBody ApplicantRequest request) {
        Long id = atsService.applyToJob(jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("applicantId", id), "Lamaran berhasil dikirim"));
    }

    // ─── Internal HR endpoints ────────────────────────────────────────────────
    @GetMapping("/api/v1/ats/jobs")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> allJobs() {
        return ResponseEntity.ok(ApiResponse.success(atsService.getAllJobs()));
    }

    @PostMapping("/api/v1/ats/jobs")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetails user) {
        Long id = atsService.createJob(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id), "Lowongan berhasil dibuat"));
    }

    @PutMapping("/api/v1/ats/jobs/{id}/publish")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> publish(@PathVariable Long id) {
        atsService.publishJob(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lowongan dipublikasikan"));
    }

    @PutMapping("/api/v1/ats/jobs/{id}/close")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Long id) {
        atsService.closeJob(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lowongan ditutup"));
    }

    @GetMapping("/api/v1/ats/jobs/{jobId}/applicants")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> applicants(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(atsService.getApplicants(jobId)));
    }

    @PutMapping("/api/v1/ats/applicants/{applicantId}/stage")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> moveStage(
            @PathVariable Long applicantId,
            @RequestParam String stage,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails user) {
        atsService.moveStage(applicantId, stage, user.getUsername(), notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Stage pelamar diperbarui"));
    }
}
