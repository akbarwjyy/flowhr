package com.flowhr.module.attendance;

import com.flowhr.common.exception.InsufficientLeaveQuotaException;
import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LeaveRepository {

    private final JdbcTemplate jdbc;

    /**
     * Kurangi kuota cuti dengan SELECT FOR UPDATE untuk mencegah race condition.
     * Sesuai aturan context.md: gunakan database-level locking.
     */
    @Transactional
    public void deductQuotaWithLock(Long employeeId, String leaveType, int days) {
        String sql = """
                SELECT remaining_days FROM leave_quotas
                WHERE employee_id = ? AND leave_type = ?::leave_type AND year = EXTRACT(YEAR FROM CURRENT_DATE)
                FOR UPDATE
                """;
        Integer remaining = jdbc.queryForObject(sql, Integer.class, employeeId, leaveType);
        if (remaining == null || remaining < days) {
            throw new InsufficientLeaveQuotaException(
                    "Sisa kuota cuti " + leaveType + " tidak mencukupi. Sisa: " + (remaining != null ? remaining : 0)
                            + " hari");
        }
        jdbc.update("""
                UPDATE leave_quotas SET
                    remaining_days = remaining_days - ?,
                    used_days = used_days + ?,
                    updated_at = NOW()
                WHERE employee_id = ? AND leave_type = ?::leave_type AND year = EXTRACT(YEAR FROM CURRENT_DATE)
                """, days, days, employeeId, leaveType);
    }

    public Long saveLeave(LeaveRequest request, Long employeeId, int totalDays) {
        return jdbc.queryForObject("""
                INSERT INTO leaves (employee_id, leave_type, start_date, end_date, total_days, reason, attachment_url)
                VALUES (?, ?::leave_type, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                employeeId, request.getLeaveType(), request.getStartDate(),
                request.getEndDate(), totalDays, request.getReason(), request.getAttachmentUrl());
    }

    public List<Map<String, Object>> findByEmployee(Long employeeId) {
        return jdbc.queryForList("""
                SELECT l.*, e.first_name, e.last_name
                FROM leaves l
                JOIN employees e ON e.id = l.employee_id
                WHERE l.employee_id = ?
                ORDER BY l.created_at DESC
                """, employeeId);
    }

    public List<Map<String, Object>> findPendingForManager(Long managerId) {
        return jdbc.queryForList("""
                SELECT l.*, e.first_name, e.last_name, e.nip, d.name AS department_name
                FROM leaves l
                JOIN employees e ON e.id = l.employee_id
                LEFT JOIN departments d ON d.id = e.department_id
                WHERE e.direct_manager_id = ? AND l.status = 'PENDING'
                ORDER BY l.created_at ASC
                """, managerId);
    }

    public void approveByManager(Long leaveId, Long managerId, String notes) {
        int updated = jdbc.update("""
                UPDATE leaves SET status = 'APPROVED_MANAGER', manager_id = ?,
                    manager_approved_at = NOW(), manager_notes = ?, updated_at = NOW()
                WHERE id = ? AND status = 'PENDING'
                """, managerId, notes, leaveId);
        if (updated == 0)
            throw new ResourceNotFoundException("Leave tidak ditemukan atau sudah diproses");
    }

    public void approveByHR(Long leaveId, Long hrId, String notes) {
        int updated = jdbc.update("""
                UPDATE leaves SET status = 'APPROVED_HR', hr_id = ?,
                    hr_approved_at = NOW(), hr_notes = ?, updated_at = NOW()
                WHERE id = ? AND status = 'APPROVED_MANAGER'
                """, hrId, notes, leaveId);
        if (updated == 0)
            throw new ResourceNotFoundException("Leave tidak ditemukan atau belum disetujui manajer");
    }

    public void reject(Long leaveId, Long reviewerId, String notes) {
        jdbc.update("""
                UPDATE leaves SET status = 'REJECTED', hr_notes = ?, updated_at = NOW()
                WHERE id = ? AND status IN ('PENDING', 'APPROVED_MANAGER')
                """, notes, leaveId);
    }

    public Map<String, Object> getQuota(Long employeeId, String leaveType) {
        return jdbc.queryForMap("""
                SELECT * FROM leave_quotas
                WHERE employee_id = ? AND leave_type = ?::leave_type AND year = EXTRACT(YEAR FROM CURRENT_DATE)
                """, employeeId, leaveType);
    }
}
