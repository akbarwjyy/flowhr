package com.flowhr.module.payroll;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final JdbcTemplate jdbc;

    public Long createBatch(int year, int month, Long departmentId) {
        return payrollRepository.createBatch(year, month, departmentId);
    }

    @Transactional
    public void processBatch(Long batchId, String username) {
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        payrollRepository.processBatch(batchId, userId);
    }

    public List<Map<String, Object>> getBatches() {
        return payrollRepository.getBatches();
    }

    public List<Map<String, Object>> getPayslipsForBatch(Long batchId) {
        return payrollRepository.getPayslipsForBatch(batchId);
    }

    public List<Map<String, Object>> getMyPayslips(String username) {
        Long employeeId = jdbc.queryForObject(
                "SELECT e.id FROM employees e JOIN users u ON u.id = e.user_id WHERE u.username = ?",
                Long.class, username);
        return payrollRepository.getMyPayslips(employeeId);
    }
}
