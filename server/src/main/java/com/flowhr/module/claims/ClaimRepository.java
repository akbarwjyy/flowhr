package com.flowhr.module.claims;

import com.flowhr.common.exception.BusinessRuleException;
import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ClaimRepository {

    private final JdbcTemplate jdbc;

    public Long submitClaim(Long employeeId, ClaimSubmitRequest request) {
        // Validasi limit kategori
        Map<String, Object> category = jdbc.queryForMap(
                "SELECT max_amount FROM claim_categories WHERE id = ? AND is_active = true",
                request.getCategoryId());
        Object maxAmount = category.get("max_amount");
        if (maxAmount != null && request.getAmount()
                .compareTo(java.math.BigDecimal.valueOf(((Number) maxAmount).doubleValue())) > 0) {
            throw new BusinessRuleException("Jumlah klaim melebihi batas kategori: " + maxAmount);
        }

        return jdbc.queryForObject(
                """
                        INSERT INTO claims (employee_id, category_id, title, description, amount, claim_date, receipt_url, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'SUBMITTED')
                        RETURNING id
                        """,
                Long.class,
                employeeId, request.getCategoryId(), request.getTitle(),
                request.getDescription(), request.getAmount(), request.getClaimDate(), request.getReceiptUrl());
    }

    public List<Map<String, Object>> findByEmployee(Long employeeId) {
        return jdbc.queryForList("""
                SELECT c.*, cc.name AS category_name
                FROM claims c
                JOIN claim_categories cc ON cc.id = c.category_id
                WHERE c.employee_id = ?
                ORDER BY c.created_at DESC
                """, employeeId);
    }

    public List<Map<String, Object>> findPendingForManager(Long managerId) {
        return jdbc.queryForList("""
                SELECT c.*, cc.name AS category_name, e.first_name, e.last_name, e.nip
                FROM claims c
                JOIN claim_categories cc ON cc.id = c.category_id
                JOIN employees e ON e.id = c.employee_id
                WHERE e.direct_manager_id = ? AND c.status = 'SUBMITTED'
                ORDER BY c.created_at ASC
                """, managerId);
    }

    public void approveByManager(Long claimId, Long managerId, String notes) {
        int updated = jdbc.update("""
                UPDATE claims SET status = 'APPROVED_MANAGER', manager_id = ?,
                    manager_approved_at = NOW(), manager_notes = ?, updated_at = NOW()
                WHERE id = ? AND status = 'SUBMITTED'
                """, managerId, notes, claimId);
        if (updated == 0)
            throw new ResourceNotFoundException("Klaim tidak ditemukan atau status tidak valid");
    }

    public void approveByHR(Long claimId, Long hrId, String notes) {
        int updated = jdbc.update("""
                UPDATE claims SET status = 'APPROVED_HR', hr_id = ?,
                    hr_approved_at = NOW(), hr_notes = ?, updated_at = NOW()
                WHERE id = ? AND status = 'APPROVED_MANAGER'
                """, hrId, notes, claimId);
        if (updated == 0)
            throw new ResourceNotFoundException("Klaim tidak ditemukan atau belum disetujui manajer");
    }

    public void reject(Long claimId, String notes) {
        jdbc.update("""
                UPDATE claims SET status = 'REJECTED', hr_notes = ?, updated_at = NOW()
                WHERE id = ?
                """, notes, claimId);
    }
}
