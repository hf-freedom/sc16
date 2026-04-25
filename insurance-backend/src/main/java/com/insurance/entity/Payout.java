package com.insurance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {
    private String id;
    private String claimId;
    private String policyId;
    private String customerId;
    private Double payoutAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
