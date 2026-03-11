package com.flowhr.module.performance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final JdbcTemplate jdbc;

    public Long createGoal(Long employeeId, GoalRequest request, String username) {
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        return performanceRepository.createGoal(employeeId, request, userId);
    }

    public List<Map<String, Object>> getGoals(Long employeeId, int year) {
        return performanceRepository.findGoalsByEmployee(employeeId, year);
    }

    public Long createAppraisal(Long employeeId, AppraisalRequest request, String reviewerUsername) {
        Long reviewerId = getEmployeeId(reviewerUsername);
        return performanceRepository.createAppraisal(employeeId, reviewerId, request);
    }

    public List<Map<String, Object>> getAppraisals(Long employeeId) {
        return performanceRepository.findAppraisalsByEmployee(employeeId);
    }

    public void submitAppraisal(Long appraisalId) {
        performanceRepository.submitAppraisal(appraisalId);
    }

    public void finalizeAppraisal(Long appraisalId, BigDecimal bonus, boolean promotion) {
        performanceRepository.finalizeAppraisal(appraisalId, bonus, promotion);
    }

    public List<Map<String, Object>> getMyGoals(String username, int year) {
        Long employeeId = getEmployeeId(username);
        return performanceRepository.findGoalsByEmployee(employeeId, year);
    }

    private Long getEmployeeId(String username) {
        return jdbc.queryForObject(
                "SELECT e.id FROM employees e JOIN users u ON u.id = e.user_id WHERE u.username = ?",
                Long.class, username);
    }
}
