package com.flowhr.module.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final JdbcTemplate jdbc;

    public void clockIn(String username, Double lat, Double lng) {
        Long employeeId = getEmployeeIdByUsername(username);
        attendanceRepository.clockIn(employeeId, lat, lng);
    }

    public void clockOut(String username, Double lat, Double lng) {
        Long employeeId = getEmployeeIdByUsername(username);
        attendanceRepository.clockOut(employeeId, lat, lng);
    }

    public List<Map<String, Object>> getMyAttendance(String username, LocalDate from, LocalDate to) {
        Long employeeId = getEmployeeIdByUsername(username);
        return attendanceRepository.findByEmployeeAndDateRange(employeeId, from, to);
    }

    public List<Map<String, Object>> getAllAttendance(LocalDate from, LocalDate to, int page, int size) {
        return attendanceRepository.findAllByDateRange(from, to, page, size);
    }

    private Long getEmployeeIdByUsername(String username) {
        return jdbc.queryForObject(
                "SELECT e.id FROM employees e JOIN users u ON u.id = e.user_id WHERE u.username = ?",
                Long.class, username);
    }
}
