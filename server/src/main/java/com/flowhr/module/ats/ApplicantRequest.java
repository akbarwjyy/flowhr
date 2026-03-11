package com.flowhr.module.ats;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApplicantRequest {
    @NotBlank
    private String firstName;
    private String lastName;
    @NotBlank
    @Email
    private String email;
    private String phone;
    private String cvUrl;
    private String linkedinUrl;
    private String source;
    private String currentCompany;
    private BigDecimal expectedSalary;
}
