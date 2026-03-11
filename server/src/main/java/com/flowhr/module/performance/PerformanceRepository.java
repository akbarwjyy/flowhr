package com.flowhr.module.performance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PerformanceRepository {

    private final JdbcTemplate jdbc;

    public Long createGoal(Long employeeId, GoalRequest request, Long createdBy) {
        return jdbc.queryForObject("""
                INSERT INTO goals (employee_id, title, description, target, metric, target_value,
                    weight, start_date, end_date, period_year, period_month, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """, Long.class,
                employeeId, request.getTitle(), request.getDescription(), request.getTarget(),
                request.getMetric(), request.getTargetValue(), request.getWeight(),
                request.getStartDate(), request.getEndDate(), request.getPeriodYear(),
                request.getPeriodMonth(), createdBy);
    }

    public List<Map<String, Object>> findGoalsByEmployee(Long employeeId, int year) {
        return jdbc.queryForList("""
                SELECT g.*, u.username AS created_by_username
                FROM goals g
                LEFT JOIN users u ON u.id = g.created_by
                WHERE g.employee_id = ? AND g.period_year = ?
                ORDER BY g.weight DESC, g.created_at DESC
                """, employeeId, year);
    }

    public Long createAppraisal(Long employeeId, Long reviewerId, AppraisalRequest request) {
        return jdbc.queryForObject("""
                INSERT INTO appraisals (employee_id, reviewer_id, period, period_year,
                    period_quarter, goal_achievement, strengths, improvements,
                    development_plan, overall_score, status)
                VALUES (?, ?, ?::appraisal_period, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')
                RETURNING id
                """, Long.class,
                employeeId, reviewerId, request.getPeriod(), request.getPeriodYear(),
                request.getPeriodQuarter(), request.getGoalAchievement(), request.getStrengths(),
                request.getImprovements(), request.getDevelopmentPlan(), request.getOverallScore());
    }

    public List<Map<String, Object>> findAppraisalsByEmployee(Long employeeId) {
        return jdbc.queryForList("""
                SELECT a.*, e.first_name AS reviewer_first_name, e.last_name AS reviewer_last_name
                FROM appraisals a
                JOIN employees e ON e.id = a.reviewer_id
                WHERE a.employee_id = ?
                ORDER BY a.period_year DESC, a.period_quarter DESC
                """, employeeId);
    }

    public void submitAppraisal(Long appraisalId) {
        jdbc.update("""
                UPDATE appraisals SET status = 'SUBMITTED', submitted_at = NOW(), updated_at = NOW()
                WHERE id = ?
                """, appraisalId);
    }

    public void finalizeAppraisal(Long appraisalId, BigDecimal bonusRecommendation, boolean promotionRecommendation) {
        jdbc.update("""
                UPDATE appraisals SET status = 'FINAL', finalized_at = NOW(),
                    bonus_recommendation = ?, promotion_recommendation = ?, updated_at = NOW()
                WHERE id = ?
                """, bonusRecommendation, promotionRecommendation, appraisalId);
    }
}
