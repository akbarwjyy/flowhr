package com.flowhr.module.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public Map<String, Object> getDashboard() {
        return analyticsRepository.getHeadcountSummary();
    }

    public List<Map<String, Object>> getHeadcountByDepartment() {
        return analyticsRepository.getHeadcountByDepartment();
    }

    public List<Map<String, Object>> getTurnoverRate(int year) {
        return analyticsRepository.getTurnoverRate(year);
    }

    public List<Map<String, Object>> getAttendanceReport(int year, int month) {
        return analyticsRepository.getAttendanceReport(year, month);
    }

    public List<Map<String, Object>> getPayrollReport(int year, int month) {
        return analyticsRepository.getPayrollReport(year, month);
    }

    public Map<String, Object> getPayrollSummary(int year, int month) {
        return analyticsRepository.getPayrollSummary(year, month);
    }
}
