package com.flowhr.module.claims;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final JdbcTemplate jdbc;

    @Transactional
    public Long submit(String username, ClaimSubmitRequest request) {
        Long employeeId = getEmployeeId(username);
        return claimRepository.submitClaim(employeeId, request);
    }

    public List<Map<String, Object>> getMyClaims(String username) {
        Long employeeId = getEmployeeId(username);
        return claimRepository.findByEmployee(employeeId);
    }

    public List<Map<String, Object>> getPendingForManager(String username) {
        Long managerId = getEmployeeId(username);
        return claimRepository.findPendingForManager(managerId);
    }

    @Transactional
    public void approveByManager(Long claimId, String username, String notes) {
        claimRepository.approveByManager(claimId, getEmployeeId(username), notes);
    }

    @Transactional
    public void approveByHR(Long claimId, String username, String notes) {
        claimRepository.approveByHR(claimId, getEmployeeId(username), notes);
    }

    public void reject(Long claimId, String notes) {
        claimRepository.reject(claimId, notes);
    }

    public List<Map<String, Object>> getCategories() {
        return jdbc.queryForList("SELECT * FROM claim_categories WHERE is_active = true ORDER BY name");
    }

    private Long getEmployeeId(String username) {
        return jdbc.queryForObject(
                "SELECT e.id FROM employees e JOIN users u ON u.id = e.user_id WHERE u.username = ?",
                Long.class, username);
    }
}
