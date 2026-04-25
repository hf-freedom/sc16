package com.insurance.service;

import com.insurance.entity.Claim;
import com.insurance.entity.Payout;
import com.insurance.entity.Policy;
import com.insurance.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PayoutService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private ClaimService claimService;
    
    @Autowired
    private PolicyService policyService;
    
    public Payout createPayout(String claimId) {
        Claim claim = claimService.getClaim(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("理赔单不存在");
        }
        
        if ("PAID".equals(claim.getStatus())) {
            throw new IllegalArgumentException("该理赔单已完成赔付，无法重复操作");
        }
        
        if (!"APPROVED".equals(claim.getStatus())) {
            throw new IllegalArgumentException("理赔单未审核通过，无法赔付");
        }
        
        List<Payout> existingPayouts = dataStore.getAllPayouts();
        Optional<Payout> existingPayout = existingPayouts.stream()
            .filter(p -> claimId.equals(p.getClaimId()))
            .findFirst();
        
        if (existingPayout.isPresent()) {
            Payout payout = existingPayout.get();
            if ("PENDING".equals(payout.getStatus())) {
                throw new IllegalArgumentException("该理赔单已有待完成的赔付单，赔付单号: " + payout.getId());
            } else if ("COMPLETED".equals(payout.getStatus())) {
                throw new IllegalArgumentException("该理赔单已完成赔付，无法重复生成赔付单");
            }
        }
        
        Policy policy = policyService.getPolicy(claim.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("保单不存在");
        }
        
        if (claim.getActualPayoutAmount() == null || claim.getActualPayoutAmount() <= 0) {
            throw new IllegalArgumentException("赔付金额无效");
        }
        
        if (policy.getRemainingAmount() == null || policy.getRemainingAmount() < claim.getActualPayoutAmount()) {
            double actualPayout = policy.getRemainingAmount() != null ? policy.getRemainingAmount() : 0.0;
            claim.setActualPayoutAmount(actualPayout);
        }
        
        Payout payout = Payout.builder()
            .claimId(claimId)
            .policyId(claim.getPolicyId())
            .customerId(claim.getCustomerId())
            .payoutAmount(claim.getActualPayoutAmount())
            .status("PENDING")
            .createdAt(LocalDateTime.now())
            .build();
        
        return dataStore.savePayout(payout);
    }
    
    public Payout completePayout(String payoutId) {
        Payout payout = dataStore.getPayout(payoutId);
        if (payout == null) {
            throw new IllegalArgumentException("赔付单不存在");
        }
        
        if (!"PENDING".equals(payout.getStatus())) {
            throw new IllegalArgumentException("当前状态不允许完成赔付");
        }
        
        Claim claim = claimService.getClaim(payout.getClaimId());
        if (claim == null) {
            throw new IllegalArgumentException("理赔单不存在");
        }
        
        Policy policy = policyService.getPolicy(payout.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("保单不存在");
        }
        
        if (policy.getRemainingAmount() == null || policy.getRemainingAmount() < payout.getPayoutAmount()) {
            double actualPayout = policy.getRemainingAmount() != null ? policy.getRemainingAmount() : 0.0;
            payout.setPayoutAmount(actualPayout);
        }
        
        policyService.reduceRemainingAmount(payout.getPolicyId(), payout.getPayoutAmount());
        
        payout.setStatus("COMPLETED");
        payout.setPaidAt(LocalDateTime.now());
        
        claim.setStatus("PAID");
        claim.setUpdatedAt(LocalDateTime.now());
        dataStore.saveClaim(claim);
        
        return dataStore.savePayout(payout);
    }
    
    public Payout getPayout(String id) {
        return dataStore.getPayout(id);
    }
    
    public List<Payout> getAllPayouts() {
        return dataStore.getAllPayouts();
    }
}
