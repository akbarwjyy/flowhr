package com.flowhr.module.corehr;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class EmployeeDto {
    private Long id;
    private String nip;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String emailPersonal;
    private String departmentName;
    private String positionTitle;
    private String employmentStatus;
    private LocalDate joinDate;
    private BigDecimal baseSalary;
    private String photoUrl;
    private Map<String, Object> customFields;
}
