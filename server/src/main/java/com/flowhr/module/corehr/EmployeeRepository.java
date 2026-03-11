package com.flowhr.module.corehr;

import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<EmployeeDto> employeeRowMapper = (rs, rowNum) -> EmployeeDto.builder()
            .id(rs.getLong("id"))
            .nip(rs.getString("nip"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .fullName(rs.getString("first_name")
                    + (rs.getString("last_name") != null ? " " + rs.getString("last_name") : ""))
            .gender(rs.getString("gender"))
            .phone(rs.getString("phone"))
            .emailPersonal(rs.getString("email_personal"))
            .departmentName(rs.getString("department_name"))
            .positionTitle(rs.getString("position_title"))
            .employmentStatus(rs.getString("employment_status"))
            .joinDate(rs.getDate("join_date") != null ? rs.getDate("join_date").toLocalDate() : null)
            .baseSalary(rs.getBigDecimal("base_salary"))
            .photoUrl(rs.getString("photo_url"))
            .build();

    private static final String BASE_SELECT = """
            SELECT e.id, e.nip, e.first_name, e.last_name, e.gender, e.phone, e.email_personal,
                   e.employment_status, e.join_date, e.base_salary, e.photo_url,
                   d.name AS department_name, p.title AS position_title
            FROM employees e
            LEFT JOIN departments d ON d.id = e.department_id
            LEFT JOIN positions p ON p.id = e.position_id
            """;

    public List<EmployeeDto> findAll(int page, int size, String search) {
        int offset = page * size;
        if (search != null && !search.isBlank()) {
            String sql = BASE_SELECT + """
                    WHERE e.employment_status != 'RESIGNED'
                    AND (e.first_name ILIKE ? OR e.last_name ILIKE ? OR e.nip ILIKE ?)
                    ORDER BY e.first_name LIMIT ? OFFSET ?
                    """;
            String q = "%" + search + "%";
            return jdbc.query(sql, employeeRowMapper, q, q, q, size, offset);
        }
        String sql = BASE_SELECT + "WHERE e.employment_status != 'RESIGNED' ORDER BY e.first_name LIMIT ? OFFSET ?";
        return jdbc.query(sql, employeeRowMapper, size, offset);
    }

    public int countAll(String search) {
        if (search != null && !search.isBlank()) {
            String q = "%" + search + "%";
            return jdbc.queryForObject(
                    "SELECT COUNT(*) FROM employees e WHERE e.employment_status != 'RESIGNED' AND (e.first_name ILIKE ? OR e.nip ILIKE ?)",
                    Integer.class, q, q);
        }
        return jdbc.queryForObject("SELECT COUNT(*) FROM employees WHERE employment_status != 'RESIGNED'",
                Integer.class);
    }

    public Optional<EmployeeDto> findById(Long id) {
        List<EmployeeDto> result = jdbc.query(BASE_SELECT + "WHERE e.id = ?", employeeRowMapper, id);
        return result.stream().findFirst();
    }

    public Long save(EmployeeRequest req) {
        String sql = """
                INSERT INTO employees (nip, first_name, last_name, gender, phone, email_personal,
                    department_id, position_id, employment_status, join_date, base_salary,
                    photo_url, direct_manager_id)
                VALUES (?, ?, ?, ?::gender_type, ?, ?, ?, ?, ?::employment_status, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getNip());
            ps.setString(2, req.getFirstName());
            ps.setString(3, req.getLastName());
            ps.setString(4, req.getGender());
            ps.setString(5, req.getPhone());
            ps.setString(6, req.getEmailPersonal());
            ps.setLong(7, req.getDepartmentId());
            ps.setLong(8, req.getPositionId());
            ps.setString(9, req.getEmploymentStatus());
            ps.setObject(10, req.getJoinDate());
            ps.setBigDecimal(11, req.getBaseSalary());
            ps.setString(12, req.getPhotoUrl());
            ps.setObject(13, req.getDirectManagerId());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void update(Long id, EmployeeRequest req) {
        int updated = jdbc.update("""
                UPDATE employees SET
                    first_name = ?, last_name = ?, gender = ?::gender_type, phone = ?,
                    email_personal = ?, department_id = ?, position_id = ?,
                    employment_status = ?::employment_status, base_salary = ?, photo_url = ?,
                    direct_manager_id = ?, updated_at = NOW()
                WHERE id = ?
                """,
                req.getFirstName(), req.getLastName(), req.getGender(), req.getPhone(),
                req.getEmailPersonal(), req.getDepartmentId(), req.getPositionId(),
                req.getEmploymentStatus(), req.getBaseSalary(), req.getPhotoUrl(),
                req.getDirectManagerId(), id);
        if (updated == 0)
            throw new ResourceNotFoundException("Employee", id);
    }

    public void deleteById(Long id) {
        // Soft delete: set employment_status to RESIGNED
        jdbc.update("UPDATE employees SET employment_status = 'RESIGNED', updated_at = NOW() WHERE id = ?", id);
    }
}
