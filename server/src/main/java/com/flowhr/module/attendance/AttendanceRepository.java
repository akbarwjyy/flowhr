package com.flowhr.module.attendance;

import com.flowhr.common.exception.BusinessRuleException;
import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AttendanceRepository {

    private final JdbcTemplate jdbc;

    public void clockIn(Long employeeId, Double lat, Double lng) {
        LocalDate today = LocalDate.now();
        // Cek apakah sudah clock-in hari ini
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM attendances WHERE employee_id = ? AND attendance_date = ?",
                Integer.class, employeeId, today);
        if (count != null && count > 0) {
            throw new BusinessRuleException("Anda sudah melakukan clock-in hari ini");
        }
        jdbc.update("""
                INSERT INTO attendances (employee_id, attendance_date, clock_in, clock_in_lat, clock_in_lng, status)
                VALUES (?, ?, NOW(), ?, ?, 'PRESENT')
                """, employeeId, today, lat, lng);
    }

    public void clockOut(Long employeeId, Double lat, Double lng) {
        LocalDate today = LocalDate.now();
        int updated = jdbc.update("""
                UPDATE attendances
                SET clock_out = NOW(),
                    clock_out_lat = ?,
                    clock_out_lng = ?,
                    work_minutes = EXTRACT(EPOCH FROM (NOW() - clock_in)) / 60,
                    updated_at = NOW()
                WHERE employee_id = ? AND attendance_date = ? AND clock_out IS NULL
                """, lat, lng, employeeId, today);
        if (updated == 0) {
            throw new BusinessRuleException("Tidak ada data clock-in hari ini atau sudah clock-out");
        }
    }

    public List<Map<String, Object>> findByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        return jdbc.queryForList("""
                SELECT a.*, e.first_name, e.last_name, e.nip
                FROM attendances a
                JOIN employees e ON e.id = a.employee_id
                WHERE a.employee_id = ? AND a.attendance_date BETWEEN ? AND ?
                ORDER BY a.attendance_date DESC
                """, employeeId, from, to);
    }

    public List<Map<String, Object>> findAllByDateRange(LocalDate from, LocalDate to, int page, int size) {
        int offset = page * size;
        return jdbc.queryForList("""
                SELECT a.attendance_date, a.clock_in, a.clock_out, a.status, a.work_minutes,
                       e.first_name, e.last_name, e.nip, d.name AS department_name
                FROM attendances a
                JOIN employees e ON e.id = a.employee_id
                LEFT JOIN departments d ON d.id = e.department_id
                WHERE a.attendance_date BETWEEN ? AND ?
                ORDER BY a.attendance_date DESC, e.first_name
                LIMIT ? OFFSET ?
                """, from, to, size, offset);
    }
}
