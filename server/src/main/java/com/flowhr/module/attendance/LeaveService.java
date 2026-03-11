package com.flowhr.module.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final JdbcTemplate jdbc;

    /**
     * Pengajuan cuti — @Transactional wajib untuk operasi pemotongan kuota.
     * Sesuai aturan context.md poin 3.
     */
    @Transactional
    public Long applyLeave(String username, LeaveRequest request) {
        Long employeeId = getEmployeeIdByUsername(username);
        int totalDays = countWorkingDays(request.getStartDate(), request.getEndDate());

        // Jenis cuti selain SICK dan UNPAID perlu cek kuota
        if (!request.getLeaveType().equals("SICK") && !request.getLeaveType().equals("UNPAID")) {
            leaveRepository.deductQuotaWithLock(employeeId, request.getLeaveType(), totalDays);
        }

        return leaveRepository.saveLeave(request, employeeId, totalDays);
    }

    public List<Map<String, Object>> getMyLeaves(String username) {
        Long employeeId = getEmployeeIdByUsername(username);
        return leaveRepository.findByEmployee(employeeId);
    }

    public List<Map<String, Object>> getPendingForManager(String username) {
        Long managerId = getEmployeeIdByUsername(username);
        return leaveRepository.findPendingForManager(managerId);
    }

    @Transactional
    public void approveByManager(Long leaveId, String username, String notes) {
        Long managerId = getEmployeeIdByUsername(username);
        leaveRepository.approveByManager(leaveId, managerId, notes);
    }

    @Transactional
    public void approveByHR(Long leaveId, String username, String notes) {
        Long hrId = getEmployeeIdByUsername(username);
        leaveRepository.approveByHR(leaveId, hrId, notes);
    }

    @Transactional
    public void reject(Long leaveId, String username, String notes) {
        Long reviewerId = getEmployeeIdByUsername(username);
        leaveRepository.reject(leaveId, reviewerId, notes);
    }

    public Map<String, Object> getQuota(String username, String leaveType) {
        Long employeeId = getEmployeeIdByUsername(username);
        return leaveRepository.getQuota(employeeId, leaveType);
    }

    private Long getEmployeeIdByUsername(String username) {
        return jdbc.queryForObject(
                "SELECT e.id FROM employees e JOIN users u ON u.id = e.user_id WHERE u.username = ?",
                Long.class, username);
    }

    private int countWorkingDays(LocalDate from, LocalDate to) {
        int days = 0;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }
}
