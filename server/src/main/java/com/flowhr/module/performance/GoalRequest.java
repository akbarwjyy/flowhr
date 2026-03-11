package com.flowhr.module.performance;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GoalRequest {
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String target;
    private String metric;
    private BigDecimal targetValue;
    private int weight = 100;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Integer periodYear;
    private Integer periodMonth;
}
