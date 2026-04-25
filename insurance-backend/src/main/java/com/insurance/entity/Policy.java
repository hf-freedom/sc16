package com.insurance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    private String id;
    private String customerId;
    private String insuranceType;
    private Double totalAmount;
    private Double remainingAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double deductible;
    private Double payoutRatio;
    private String status;
    private List<String> coverageTypes;
}
