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
    private String birthPlace;
    private String maritalStatus;
    private String religion;
    private String nationality;
    private String phone;
    private String emailPersonal;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String nik;
    private String npwp;
    private String bpjsKes;
    private String bpjsTk;
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionTitle;
    private String employmentStatus;
    private LocalDate joinDate;
    private LocalDate endDate;
    private BigDecimal baseSalary;
    private String photoUrl;
    private Map<String, Object> customFields;
}
