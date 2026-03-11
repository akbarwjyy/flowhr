package com.flowhr.module.performance;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AppraisalRequest {
    private String period = "QUARTERLY"; // QUARTERLY or ANNUAL
    private Integer periodYear;
    private Integer periodQuarter;
    private BigDecimal overallScore;
    private String goalAchievement;
    private String strengths;
    private String improvements;
    private String developmentPlan;
}
