package com.flowhr.module.payroll;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PayrollRepository {

    private final JdbcTemplate jdbc;

    public Long createBatch(int year, int month, Long departmentId) {
        return jdbc.queryForObject("""
                INSERT INTO payroll_batches (period_year, period_month, department_id, status)
                VALUES (?, ?, ?, 'DRAFT')
                RETURNING id
                """, Long.class, year, month, departmentId);
    }

    /**
     * Kalkulasi dan generate payslip per karyawan. @Transactional wajib (context.md
     * poin 3).
     */
    @Transactional
    public void processBatch(Long batchId, Long processedByUserId) {
        // Ambil semua karyawan aktif untuk batch ini
        String employeeSql = """
                SELECT e.id, e.base_salary, e.nip
                FROM employees e
                JOIN payroll_batches pb ON pb.id = ?
                WHERE e.employment_status = 'ACTIVE'
                  AND (pb.department_id IS NULL OR e.department_id = pb.department_id)
                """;

        List<Map<String, Object>> employees = jdbc.queryForList(employeeSql, batchId);

        for (Map<String, Object> emp : employees) {
            Long employeeId = (Long) emp.get("id");
            BigDecimal baseSalary = (BigDecimal) emp.get("base_salary");
            String nip = (String) emp.get("nip");

            // Reimbursement dari klaim yang sudah disetujui HR
            BigDecimal reimbursement = jdbc.queryForObject("""
                    SELECT COALESCE(SUM(c.amount), 0)
                    FROM claims c
                    JOIN payroll_batches pb ON pb.id = ?
                    WHERE c.employee_id = ?
                      AND c.status = 'APPROVED_HR'
                      AND c.payroll_batch_id IS NULL
                      AND EXTRACT(YEAR FROM c.claim_date) = pb.period_year
                      AND EXTRACT(MONTH FROM c.claim_date) = pb.period_month
                    """, BigDecimal.class, batchId, employeeId);

            // Keterlambatan (menit keterlambatan * tarif)
            BigDecimal lateDeduction = jdbc.queryForObject("""
                    SELECT COALESCE(SUM(GREATEST(
                        EXTRACT(EPOCH FROM (a.clock_in - (a.attendance_date + '08:00:00'::time))) / 60, 0
                    )), 0) * 2000
                    FROM attendances a
                    JOIN payroll_batches pb ON pb.id = ?
                    WHERE a.employee_id = ?
                      AND a.status = 'LATE'
                      AND EXTRACT(YEAR FROM a.attendance_date) = pb.period_year
                      AND EXTRACT(MONTH FROM a.attendance_date) = pb.period_month
                    """, BigDecimal.class, batchId, employeeId);

            // Tunjangan makan (Rp 30.000/hari kerja hadir)
            Integer workDays = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM attendances a
                    JOIN payroll_batches pb ON pb.id = ?
                    WHERE a.employee_id = ? AND a.status IN ('PRESENT', 'LATE')
                      AND EXTRACT(YEAR FROM a.attendance_date) = pb.period_year
                      AND EXTRACT(MONTH FROM a.attendance_date) = pb.period_month
                    """, Integer.class, batchId, employeeId);

            BigDecimal mealAllowance = BigDecimal.valueOf(workDays != null ? workDays * 30000L : 0);

            // PPh21 sederhana: 5% dari (gaji pokok - 4.500.000) jika > 0
            BigDecimal taxableIncome = baseSalary.subtract(new BigDecimal("4500000"));
            BigDecimal pph21 = taxableIncome.compareTo(BigDecimal.ZERO) > 0
                    ? taxableIncome.multiply(new BigDecimal("0.05"))
                    : BigDecimal.ZERO;

            // BPJS: 1% + 2% dari gaji pokok
            BigDecimal bpjsKes = baseSalary.multiply(new BigDecimal("0.01"));
            BigDecimal bpjsTk = baseSalary.multiply(new BigDecimal("0.02"));

            BigDecimal grossSalary = baseSalary.add(mealAllowance)
                    .add(reimbursement != null ? reimbursement : BigDecimal.ZERO);
            BigDecimal totalDeduction = lateDeduction.add(pph21).add(bpjsKes).add(bpjsTk);
            BigDecimal netSalary = grossSalary.subtract(totalDeduction);

            String pdfPassword = "FLOWHR_" + nip;

            // Insert payslip
            jdbc.update("""
                    INSERT INTO payslips (
                        batch_id, employee_id, base_salary, meal_allowance, reimbursement_total,
                        late_deduction, pph21, bpjs_kes_employee, bpjs_tk_employee,
                        gross_salary, total_deduction, net_salary, pdf_password, status
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'GENERATED')
                    ON CONFLICT (batch_id, employee_id) DO NOTHING
                    """,
                    batchId, employeeId, baseSalary, mealAllowance,
                    reimbursement, lateDeduction, pph21, bpjsKes, bpjsTk,
                    grossSalary, totalDeduction, netSalary, pdfPassword);

            // Link klaim ke batch ini
            jdbc.update("""
                    UPDATE claims SET payroll_batch_id = ?
                    WHERE employee_id = ? AND status = 'APPROVED_HR' AND payroll_batch_id IS NULL
                    """, batchId, employeeId);
        }

        // Update batch status
        jdbc.update("""
                UPDATE payroll_batches SET status = 'DONE', processed_by = ?, processed_at = NOW(), updated_at = NOW()
                WHERE id = ?
                """, processedByUserId, batchId);
    }

    public List<Map<String, Object>> getPayslipsForBatch(Long batchId) {
        return jdbc.queryForList("""
                SELECT ps.*, e.first_name, e.last_name, e.nip, d.name AS department_name
                FROM payslips ps
                JOIN employees e ON e.id = ps.employee_id
                LEFT JOIN departments d ON d.id = e.department_id
                WHERE ps.batch_id = ?
                ORDER BY e.first_name
                """, batchId);
    }

    public List<Map<String, Object>> getMyPayslips(Long employeeId) {
        return jdbc.queryForList("""
                SELECT ps.*, pb.period_year, pb.period_month
                FROM payslips ps
                JOIN payroll_batches pb ON pb.id = ps.batch_id
                WHERE ps.employee_id = ? AND pb.status = 'DONE'
                ORDER BY pb.period_year DESC, pb.period_month DESC
                """, employeeId);
    }

    public List<Map<String, Object>> getBatches() {
        return jdbc.queryForList("""
                SELECT pb.*, d.name AS department_name,
                       COUNT(ps.id) AS total_payslips
                FROM payroll_batches pb
                LEFT JOIN departments d ON d.id = pb.department_id
                LEFT JOIN payslips ps ON ps.batch_id = pb.id
                GROUP BY pb.id, d.name
                ORDER BY pb.period_year DESC, pb.period_month DESC
                """);
    }
}
