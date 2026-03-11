package com.flowhr.module.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * AnalyticsRepository — pure read queries untuk executive dashboard &
 * reporting.
 */
@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private final JdbcTemplate jdbc;

    /**
     * Executive dashboard: headcount, turnover rate, gender ratio, employment
     * status breakdown.
     */
    public Map<String, Object> getHeadcountSummary() {
        return jdbc.queryForMap("""
                SELECT
                    COUNT(*) FILTER (WHERE employment_status = 'ACTIVE') AS active_count,
                    COUNT(*) FILTER (WHERE employment_status = 'PROBATION') AS probation_count,
                    COUNT(*) FILTER (WHERE employment_status = 'RESIGNED') AS resigned_count,
                    COUNT(*) FILTER (WHERE employment_status = 'TERMINATED') AS terminated_count,
                    COUNT(*) FILTER (WHERE gender = 'MALE') AS male_count,
                    COUNT(*) FILTER (WHERE gender = 'FEMALE') AS female_count,
                    EXTRACT(YEAR FROM AGE(NOW(), MIN(join_date))) AS company_age_years
                FROM employees
                WHERE employment_status != 'RESIGNED'
                """);
    }

    public List<Map<String, Object>> getHeadcountByDepartment() {
        return jdbc.queryForList("""
                SELECT d.name AS department, COUNT(e.id) AS employee_count,
                       COUNT(e.id) FILTER (WHERE e.gender = 'MALE') AS male_count,
                       COUNT(e.id) FILTER (WHERE e.gender = 'FEMALE') AS female_count
                FROM departments d
                LEFT JOIN employees e ON e.department_id = d.id AND e.employment_status IN ('ACTIVE', 'PROBATION')
                GROUP BY d.id, d.name
                ORDER BY employee_count DESC
                """);
    }

    public List<Map<String, Object>> getTurnoverRate(int year) {
        return jdbc.queryForList("""
                SELECT
                    EXTRACT(MONTH FROM join_date) AS month,
                    COUNT(*) FILTER (WHERE employment_status NOT IN ('RESIGNED', 'TERMINATED')) AS new_hires,
                    COUNT(*) FILTER (WHERE employment_status IN ('RESIGNED', 'TERMINATED')) AS departures
                FROM employees
                WHERE EXTRACT(YEAR FROM join_date) = ?
                GROUP BY EXTRACT(MONTH FROM join_date)
                ORDER BY month
                """, year);
    }

    /**
     * Attendance report per period — rekap kehadiran harian.
     */
    public List<Map<String, Object>> getAttendanceReport(int year, int month) {
        return jdbc.queryForList("""
                SELECT e.nip, e.first_name, e.last_name, d.name AS department,
                       COUNT(a.id) AS total_days,
                       COUNT(a.id) FILTER (WHERE a.status = 'PRESENT') AS present_days,
                       COUNT(a.id) FILTER (WHERE a.status = 'LATE') AS late_days,
                       COUNT(a.id) FILTER (WHERE a.status = 'ABSENCE') AS absence_days,
                       COALESCE(SUM(a.work_minutes), 0) AS total_work_minutes,
                       COALESCE(SUM(a.overtime_minutes), 0) AS total_overtime_minutes
                FROM employees e
                LEFT JOIN attendances a ON a.employee_id = e.id
                    AND EXTRACT(YEAR FROM a.attendance_date) = ?
                    AND EXTRACT(MONTH FROM a.attendance_date) = ?
                LEFT JOIN departments d ON d.id = e.department_id
                WHERE e.employment_status IN ('ACTIVE', 'PROBATION')
                GROUP BY e.id, e.nip, e.first_name, e.last_name, d.name
                ORDER BY d.name, e.first_name
                """, year, month);
    }

    /**
     * Payroll report — total pengeluaran gaji per departemen per bulan.
     */
    public List<Map<String, Object>> getPayrollReport(int year, int month) {
        return jdbc.queryForList("""
                SELECT d.name AS department,
                       COUNT(ps.id) AS employee_count,
                       SUM(ps.base_salary) AS total_base_salary,
                       SUM(ps.meal_allowance) AS total_meal_allowance,
                       SUM(ps.reimbursement_total) AS total_reimbursement,
                       SUM(ps.gross_salary) AS total_gross_salary,
                       SUM(ps.total_deduction) AS total_deduction,
                       SUM(ps.net_salary) AS total_net_salary
                FROM payslips ps
                JOIN payroll_batches pb ON pb.id = ps.batch_id
                JOIN employees e ON e.id = ps.employee_id
                LEFT JOIN departments d ON d.id = e.department_id
                WHERE pb.period_year = ? AND pb.period_month = ? AND pb.status = 'DONE'
                GROUP BY d.id, d.name
                ORDER BY total_net_salary DESC
                """, year, month);
    }

    public Map<String, Object> getPayrollSummary(int year, int month) {
        return jdbc.queryForMap("""
                SELECT
                    COUNT(ps.id) AS total_employees,
                    SUM(ps.gross_salary) AS total_gross_salary,
                    SUM(ps.total_deduction) AS total_deduction,
                    SUM(ps.net_salary) AS total_net_salary,
                    SUM(ps.pph21) AS total_pph21,
                    SUM(ps.bpjs_kes_employee) AS total_bpjs_kes,
                    SUM(ps.bpjs_tk_employee) AS total_bpjs_tk
                FROM payslips ps
                JOIN payroll_batches pb ON pb.id = ps.batch_id
                WHERE pb.period_year = ? AND pb.period_month = ? AND pb.status = 'DONE'
                """, year, month);
    }
}
