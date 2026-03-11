package com.flowhr.module.onboarding;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OnboardingRepository {

    private final JdbcTemplate jdbc;

    @Transactional
    public Long createProcess(Long employeeId, String eventType, LocalDate startDate) {
        Long processId = jdbc.queryForObject("""
                INSERT INTO onboarding_processes (employee_id, event_type, start_date)
                VALUES (?, ?::onboarding_event, ?)
                RETURNING id
                """, Long.class, employeeId, eventType, startDate);

        // Auto-create tasks from templates
        jdbc.update("""
                INSERT INTO onboarding_task_assignments
                    (process_id, template_id, title, description, responsible_dept)
                SELECT ?, t.id, t.title, t.description, t.responsible_dept
                FROM onboarding_task_templates t
                WHERE t.event_type = ?::onboarding_event AND t.is_active = true
                ORDER BY t.order_index
                """, processId, eventType);

        return processId;
    }

    public List<Map<String, Object>> findTasksByProcess(Long processId) {
        return jdbc.queryForList("""
                SELECT ta.*, u.username AS assigned_username
                FROM onboarding_task_assignments ta
                LEFT JOIN users u ON u.id = ta.assigned_to
                WHERE ta.process_id = ?
                ORDER BY ta.id
                """, processId);
    }

    public void updateTaskStatus(Long taskId, String status, String notes) {
        jdbc.update("""
                UPDATE onboarding_task_assignments
                SET status = ?::task_status,
                    notes = ?,
                    completed_at = CASE WHEN ? = 'COMPLETED' THEN NOW() ELSE NULL END,
                    updated_at = NOW()
                WHERE id = ?
                """, status, notes, status, taskId);
    }

    public List<Map<String, Object>> findByEmployee(Long employeeId) {
        return jdbc.queryForList("""
                SELECT op.*, e.first_name, e.last_name,
                       COUNT(ta.id) AS total_tasks,
                       SUM(CASE WHEN ta.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_tasks
                FROM onboarding_processes op
                JOIN employees e ON e.id = op.employee_id
                LEFT JOIN onboarding_task_assignments ta ON ta.process_id = op.id
                WHERE op.employee_id = ?
                GROUP BY op.id, e.first_name, e.last_name
                ORDER BY op.created_at DESC
                """, employeeId);
    }
}
