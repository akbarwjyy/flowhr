package com.flowhr.module.ats;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AtsRepository {

    private final JdbcTemplate jdbc;

    public Long createJob(JobRequest request, Long createdBy) {
        return jdbc.queryForObject("""
                INSERT INTO jobs (title, department_id, employment_type, location, description,
                    requirements, salary_min, salary_max, is_salary_visible, status, created_by)
                VALUES (?, ?, ?::employment_type, ?, ?, ?, ?, ?, ?, 'DRAFT', ?)
                RETURNING id
                """, Long.class,
                request.getTitle(), request.getDepartmentId(), request.getEmploymentType(),
                request.getLocation(), request.getDescription(), request.getRequirements(),
                request.getSalaryMin(), request.getSalaryMax(), request.isSalaryVisible(), createdBy);
    }

    public void publishJob(Long jobId) {
        jdbc.update("UPDATE jobs SET status = 'OPEN', published_at = NOW(), updated_at = NOW() WHERE id = ?", jobId);
    }

    public void closeJob(Long jobId) {
        jdbc.update("UPDATE jobs SET status = 'CLOSED', closed_at = NOW(), updated_at = NOW() WHERE id = ?", jobId);
    }

    public List<Map<String, Object>> findOpenJobs() {
        return jdbc.queryForList("""
                SELECT j.*, d.name AS department_name,
                       COUNT(a.id) AS applicant_count
                FROM jobs j
                LEFT JOIN departments d ON d.id = j.department_id
                LEFT JOIN applicants a ON a.job_id = j.id
                WHERE j.status = 'OPEN'
                GROUP BY j.id, d.name
                ORDER BY j.published_at DESC
                """);
    }

    public List<Map<String, Object>> findAllJobs() {
        return jdbc.queryForList("""
                SELECT j.*, d.name AS department_name,
                       COUNT(a.id) AS applicant_count
                FROM jobs j
                LEFT JOIN departments d ON d.id = j.department_id
                LEFT JOIN applicants a ON a.job_id = j.id
                GROUP BY j.id, d.name
                ORDER BY j.created_at DESC
                """);
    }

    public Long addApplicant(Long jobId, ApplicantRequest request) {
        return jdbc.queryForObject("""
                INSERT INTO applicants (job_id, first_name, last_name, email, phone,
                    cv_url, linkedin_url, source, current_company, expected_salary)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """, Long.class,
                jobId, request.getFirstName(), request.getLastName(), request.getEmail(),
                request.getPhone(), request.getCvUrl(), request.getLinkedinUrl(),
                request.getSource(), request.getCurrentCompany(), request.getExpectedSalary());
    }

    public List<Map<String, Object>> findApplicantsByJob(Long jobId) {
        return jdbc.queryForList("""
                SELECT a.*, i.scheduled_at AS next_interview, i.interview_type AS next_interview_type
                FROM applicants a
                LEFT JOIN interviews i ON i.applicant_id = a.id
                    AND i.scheduled_at > NOW() AND i.status = 'SCHEDULED'
                WHERE a.job_id = ?
                ORDER BY a.applied_at DESC
                """, jobId);
    }

    public void moveStage(Long applicantId, String newStage, Long changedBy, String notes) {
        // Record old stage
        String oldStage = jdbc.queryForObject("SELECT stage FROM applicants WHERE id = ?", String.class, applicantId);
        // Update stage
        jdbc.update("UPDATE applicants SET stage = ?::applicant_stage, updated_at = NOW() WHERE id = ?", newStage,
                applicantId);
        // Insert history
        jdbc.update("""
                INSERT INTO applicant_stage_history (applicant_id, from_stage, to_stage, changed_by, notes)
                VALUES (?, ?::applicant_stage, ?::applicant_stage, ?, ?)
                """, applicantId, oldStage, newStage, changedBy, notes);
    }
}
