package com.flowhr.module.ats;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AtsService {

    private final AtsRepository atsRepository;
    private final JdbcTemplate jdbc;

    public Long createJob(JobRequest request, String username) {
        Long userId = getUserId(username);
        return atsRepository.createJob(request, userId);
    }

    public void publishJob(Long jobId) {
        atsRepository.publishJob(jobId);
    }

    public void closeJob(Long jobId) {
        atsRepository.closeJob(jobId);
    }

    public List<Map<String, Object>> getOpenJobs() {
        return atsRepository.findOpenJobs();
    }

    public List<Map<String, Object>> getAllJobs() {
        return atsRepository.findAllJobs();
    }

    public Long applyToJob(Long jobId, ApplicantRequest request) {
        return atsRepository.addApplicant(jobId, request);
    }

    public List<Map<String, Object>> getApplicants(Long jobId) {
        return atsRepository.findApplicantsByJob(jobId);
    }

    public void moveStage(Long applicantId, String stage, String username, String notes) {
        Long userId = getUserId(username);
        atsRepository.moveStage(applicantId, stage, userId, notes);
    }

    private Long getUserId(String username) {
        return jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }
}
