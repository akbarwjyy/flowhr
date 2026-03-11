package com.flowhr.module.ats;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobRequest {
    @NotBlank
    private String title;
    private Long departmentId;
    private String employmentType = "FULL_TIME";
    private String location;
    @NotBlank
    private String description;
    private String requirements;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private boolean salaryVisible = false;
}
