package com.flowhr.module.corehr;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "NIP wajib diisi")
    private String nip;

    @NotBlank(message = "Nama depan wajib diisi")
    private String firstName;

    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private String birthPlace;
    private String phone;
    private String emailPersonal;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String nik;
    private String npwp;

    @NotNull(message = "Departemen wajib diisi")
    private Long departmentId;

    @NotNull(message = "Posisi wajib diisi")
    private Long positionId;

    @NotNull(message = "Tanggal bergabung wajib diisi")
    private LocalDate joinDate;

    @NotNull(message = "Gaji pokok wajib diisi")
    @DecimalMin(value = "0.0", inclusive = false, message = "Gaji harus lebih dari 0")
    private BigDecimal baseSalary;

    private String employmentStatus = "PROBATION";
    private Long directManagerId;
    private String photoUrl;
}
