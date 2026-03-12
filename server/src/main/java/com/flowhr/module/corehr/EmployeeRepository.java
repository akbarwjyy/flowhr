package com.flowhr.module.corehr;

import com.flowhr.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    private final RowMapper<EmployeeDto> employeeRowMapper = (rs, rowNum) -> EmployeeDto.builder()
            .id(rs.getLong("id"))
            .nip(rs.getString("nip"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .fullName(rs.getString("first_name")
                    + (rs.getString("last_name") != null ? " " + rs.getString("last_name") : ""))
            .gender(rs.getString("gender"))
            .birthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null)
            .birthPlace(rs.getString("birth_place"))
            .maritalStatus(rs.getString("marital_status"))
            .religion(rs.getString("religion"))
            .nationality(rs.getString("nationality"))
            .phone(rs.getString("phone"))
            .emailPersonal(rs.getString("email_personal"))
            .address(rs.getString("address"))
            .city(rs.getString("city"))
            .province(rs.getString("province"))
            .postalCode(rs.getString("postal_code"))
            .nik(rs.getString("nik"))
            .npwp(rs.getString("npwp"))
            .bpjsKes(rs.getString("bpjs_kes"))
            .bpjsTk(rs.getString("bpjs_tk"))
            .departmentId(rs.getLong("department_id"))
            .departmentName(rs.getString("department_name"))
            .positionId(rs.getLong("position_id"))
            .positionTitle(rs.getString("position_title"))
            .employmentStatus(rs.getString("employment_status"))
            .joinDate(rs.getDate("join_date") != null ? rs.getDate("join_date").toLocalDate() : null)
            .endDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null)
            .baseSalary(rs.getBigDecimal("base_salary"))
            .photoUrl(rs.getString("photo_url"))
            .build();

    private static final String BASE_SELECT = """
            SELECT e.*, d.name AS department_name, p.title AS position_title
            FROM employees e
            LEFT JOIN departments d ON d.id = e.department_id
            LEFT JOIN positions p ON p.id = e.position_id
            """;

    public List<EmployeeDto> findAll(int page, int size, String search) {
        int offset = page * size;
        if (search != null && !search.isBlank()) {
            String sql = BASE_SELECT + """
                    WHERE e.employment_status != 'RESIGNED'
                    AND (e.first_name ILIKE :search OR e.last_name ILIKE :search OR e.nip ILIKE :search OR e.phone ILIKE :search)
                    ORDER BY e.first_name LIMIT :limit OFFSET :offset
                    """;
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("search", "%" + search + "%")
                    .addValue("limit", size)
                    .addValue("offset", offset);
            return namedJdbc.query(sql, params, employeeRowMapper);
        }
        String sql = BASE_SELECT + "WHERE e.employment_status != 'RESIGNED' ORDER BY e.first_name LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", size)
                .addValue("offset", offset);
        return namedJdbc.query(sql, params, employeeRowMapper);
    }

    public int countAll(String search) {
        if (search != null && !search.isBlank()) {
            String sql = "SELECT COUNT(*) FROM employees e WHERE e.employment_status != 'RESIGNED' AND (e.first_name ILIKE :search OR e.nip ILIKE :search)";
            MapSqlParameterSource params = new MapSqlParameterSource("search", "%" + search + "%");
            return namedJdbc.queryForObject(sql, params, Integer.class);
        }
        return jdbc.queryForObject("SELECT COUNT(*) FROM employees WHERE employment_status != 'RESIGNED'", Integer.class);
    }

    public Optional<EmployeeDto> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<EmployeeDto> result = namedJdbc.query(BASE_SELECT + "WHERE e.id = :id", params, employeeRowMapper);
        return result.stream().findFirst();
    }

    public Long save(EmployeeRequest req) {
        String sql = """
                INSERT INTO employees (
                    nip, first_name, last_name, gender, phone, email_personal,
                    department_id, position_id, employment_status, join_date, base_salary,
                    photo_url, direct_manager_id, nik, npwp, birth_date, birth_place,
                    address, city, province, postal_code, marital_status, religion,
                    nationality, bpjs_kes, bpjs_tk, end_date
                ) VALUES (
                    :nip, :first_name, :last_name, :gender, :phone, :email_personal,
                    :department_id, :position_id, :employment_status, :join_date, :base_salary,
                    :photo_url, :direct_manager_id, :nik, :npwp, :birth_date, :birth_place,
                    :address, :city, :province, :postal_code, :marital_status, :religion,
                    :nationality, :bpjs_kes, :bpjs_tk, :end_date
                ) RETURNING id
                """;

        log.info("Saving employee: {} {}, NIK: {}, NPWP: {}", req.getFirstName(), req.getLastName(), req.getNik(), req.getNpwp());

        MapSqlParameterSource params = getParams(req);
        return namedJdbc.queryForObject(sql, params, Long.class);
    }

    public void update(Long id, EmployeeRequest req) {
        String sql = """
                UPDATE employees SET
                    first_name = :first_name, 
                    last_name = :last_name, 
                    gender = :gender, 
                    phone = :phone,
                    email_personal = :email_personal, 
                    department_id = :department_id, 
                    position_id = :position_id,
                    employment_status = :employment_status, 
                    base_salary = :base_salary, 
                    photo_url = :photo_url,
                    direct_manager_id = :direct_manager_id, 
                    nik = :nik, 
                    npwp = :npwp, 
                    birth_date = :birth_date,
                    birth_place = :birth_place, 
                    address = :address, 
                    city = :city, 
                    province = :province,
                    postal_code = :postal_code, 
                    marital_status = :marital_status, 
                    religion = :religion,
                    nationality = :nationality, 
                    bpjs_kes = :bpjs_kes, 
                    bpjs_tk = :bpjs_tk, 
                    end_date = :end_date,
                    updated_at = NOW()
                WHERE id = :id
                """;

        log.info("Updating employee ID {}: {} {}, NIK: {}", id, req.getFirstName(), req.getLastName(), req.getNik());

        MapSqlParameterSource params = getParams(req);
        params.addValue("id", id);

        int updated = namedJdbc.update(sql, params);
        if (updated == 0)
            throw new ResourceNotFoundException("Employee", id);
    }

    private MapSqlParameterSource getParams(EmployeeRequest req) {
        return new MapSqlParameterSource()
                .addValue("nip", req.getNip(), Types.VARCHAR)
                .addValue("first_name", req.getFirstName(), Types.VARCHAR)
                .addValue("last_name", req.getLastName(), Types.VARCHAR)
                .addValue("gender", req.getGender(), Types.OTHER)
                .addValue("phone", req.getPhone(), Types.VARCHAR)
                .addValue("email_personal", req.getEmailPersonal(), Types.VARCHAR)
                .addValue("department_id", req.getDepartmentId(), Types.BIGINT)
                .addValue("position_id", req.getPositionId(), Types.BIGINT)
                .addValue("employment_status", req.getEmploymentStatus(), Types.OTHER)
                .addValue("join_date", req.getJoinDate(), Types.DATE)
                .addValue("base_salary", req.getBaseSalary(), Types.NUMERIC)
                .addValue("photo_url", req.getPhotoUrl(), Types.VARCHAR)
                .addValue("direct_manager_id", req.getDirectManagerId(), Types.BIGINT)
                .addValue("nik", req.getNik(), Types.VARCHAR)
                .addValue("npwp", req.getNpwp(), Types.VARCHAR)
                .addValue("birth_date", req.getBirthDate(), Types.DATE)
                .addValue("birth_place", req.getBirthPlace(), Types.VARCHAR)
                .addValue("address", req.getAddress(), Types.VARCHAR)
                .addValue("city", req.getCity(), Types.VARCHAR)
                .addValue("province", req.getProvince(), Types.VARCHAR)
                .addValue("postal_code", req.getPostalCode(), Types.VARCHAR)
                .addValue("marital_status", req.getMaritalStatus(), Types.OTHER)
                .addValue("religion", req.getReligion(), Types.VARCHAR)
                .addValue("nationality", req.getNationality(), Types.VARCHAR)
                .addValue("bpjs_kes", req.getBpjsKes(), Types.VARCHAR)
                .addValue("bpjs_tk", req.getBpjsTk(), Types.VARCHAR)
                .addValue("end_date", req.getEndDate(), Types.DATE);
    }

    public void deleteById(Long id) {
        jdbc.update("UPDATE employees SET employment_status = 'RESIGNED', updated_at = NOW() WHERE id = ?", id);
    }
}
