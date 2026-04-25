package com.insurance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {
    private String id;
    private String policyId;
    private String customerId;
    private LocalDateTime accidentTime;
    private String accidentType;
    private String accidentDescription;
    private Double requestedAmount;
    private Double deductibleApplied;
    private Double ratioApplied;
    private Double calculatedAmount;
    private Double actualPayoutAmount;
    private List<String> materials;
    private String status;
    private Boolean needsManualReview;
    private Boolean isManualReviewed;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
