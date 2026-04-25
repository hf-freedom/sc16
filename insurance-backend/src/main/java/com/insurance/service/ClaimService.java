package com.insurance.service;

import com.insurance.entity.Claim;
import com.insurance.entity.Policy;
import com.insurance.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Autowired
    private PolicyService policyService;
    
    private static final double HIGH_AMOUNT_THRESHOLD = 50000.0;
    private static final List<String> REQUIRED_MATERIALS = Arrays.asList(
        "事故证明",
        "身份证复印件",
        "费用发票"
    );
    
    private static final List<String> IN_PROGRESS_STATUSES = Arrays.asList(
        "PENDING_MATERIALS",
        "PENDING_REVIEW",
        "APPROVED"
    );
    
    public Claim submitClaim(Claim claimRequest) {
        Policy policy = policyService.getPolicy(claimRequest.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("保单不存在");
        }
        
        if (!"ACTIVE".equals(policy.getStatus())) {
            throw new IllegalArgumentException("保单状态无效");
        }
        
        if (policy.getRemainingAmount() == null || policy.getRemainingAmount() <= 0) {
            throw new IllegalArgumentException("保单剩余额度为0，无法提交理赔申请");
        }
        
        boolean hasInProgressClaim = checkInProgressClaim(claimRequest.getPolicyId());
        if (hasInProgressClaim) {
            throw new IllegalArgumentException("该保单存在正在流程中的理赔申请，请先完成当前流程后再申请新理赔");
        }
        
        if (claimRequest.getAccidentTime() == null) {
            throw new IllegalArgumentException("事故时间不能为空");
        }
        
        LocalDate accidentDate = claimRequest.getAccidentTime().toLocalDate();
        if (accidentDate.isBefore(policy.getStartDate()) || accidentDate.isAfter(policy.getEndDate())) {
            throw new IllegalArgumentException("事故时间不在保单有效期内");
        }
        
        if (policy.getCoverageTypes() == null || !policy.getCoverageTypes().contains(claimRequest.getAccidentType())) {
            throw new IllegalArgumentException("事故类型不在保单责任范围内");
        }
        
        boolean hasDuplicate = checkDuplicateClaim(claimRequest.getPolicyId(), claimRequest.getAccidentTime(), claimRequest.getAccidentType());
        if (hasDuplicate) {
            throw new IllegalArgumentException("同一事故不能重复理赔");
        }
        
        double calculatedAmount = calculateClaimAmount(
            claimRequest.getRequestedAmount(),
            policy.getDeductible(),
            policy.getPayoutRatio()
        );
        
        Claim claim = Claim.builder()
            .policyId(claimRequest.getPolicyId())
            .customerId(policy.getCustomerId())
            .accidentTime(claimRequest.getAccidentTime())
            .accidentType(claimRequest.getAccidentType())
            .accidentDescription(claimRequest.getAccidentDescription())
            .requestedAmount(claimRequest.getRequestedAmount())
            .deductibleApplied(policy.getDeductible())
            .ratioApplied(policy.getPayoutRatio())
            .calculatedAmount(calculatedAmount)
            .materials(claimRequest.getMaterials())
            .status("PENDING_MATERIALS")
            .needsManualReview(calculatedAmount >= HIGH_AMOUNT_THRESHOLD)
            .isManualReviewed(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        if (claim.getMaterials() != null && hasAllRequiredMaterials(claim.getMaterials())) {
            claim.setStatus("PENDING_REVIEW");
        }
        
        return dataStore.saveClaim(claim);
    }
    
    public Claim getClaim(String id) {
        return dataStore.getClaim(id);
    }
    
    public List<Claim> getAllClaims() {
        return dataStore.getAllClaims();
    }
    
    public Claim addMaterials(String claimId, List<String> materials) {
        Claim claim = dataStore.getClaim(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("理赔单不存在");
        }
        
        if (!"PENDING_MATERIALS".equals(claim.getStatus())) {
            throw new IllegalArgumentException("当前状态不允许补充材料");
        }
        
        if (claim.getMaterials() != null) {
            claim.getMaterials().addAll(materials);
        } else {
            claim.setMaterials(materials);
        }
        
        if (hasAllRequiredMaterials(claim.getMaterials())) {
            claim.setStatus("PENDING_REVIEW");
        }
        
        claim.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveClaim(claim);
    }
    
    public Claim reviewClaim(String claimId, Boolean approved, String comment) {
        Claim claim = dataStore.getClaim(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("理赔单不存在");
        }
        
        if (claim.getNeedsManualReview() != null && claim.getNeedsManualReview() && !Boolean.TRUE.equals(claim.getIsManualReviewed())) {
            claim.setIsManualReviewed(approved);
            claim.setReviewComment(comment);
            claim.setUpdatedAt(LocalDateTime.now());
            
            if (Boolean.TRUE.equals(approved)) {
                claim.setStatus("PENDING_REVIEW");
            } else {
                claim.setStatus("REJECTED");
            }
            
            return dataStore.saveClaim(claim);
        }
        
        if (!"PENDING_REVIEW".equals(claim.getStatus())) {
            throw new IllegalArgumentException("当前状态不允许审核");
        }
        
        if (Boolean.TRUE.equals(approved)) {
            Policy policy = policyService.getPolicy(claim.getPolicyId());
            if (policy == null) {
                throw new IllegalArgumentException("保单不存在");
            }
            
            double calculatedAmount = claim.getCalculatedAmount();
            double remainingAmount = policy.getRemainingAmount() != null ? policy.getRemainingAmount() : 0.0;
            
            double actualPayout = Math.min(calculatedAmount, remainingAmount);
            claim.setActualPayoutAmount(actualPayout);
            claim.setStatus("APPROVED");
        } else {
            claim.setStatus("REJECTED");
            claim.setReviewComment(comment);
        }
        
        claim.setUpdatedAt(LocalDateTime.now());
        return dataStore.saveClaim(claim);
    }
    
    private boolean hasAllRequiredMaterials(List<String> materials) {
        if (materials == null || materials.isEmpty()) {
            return false;
        }
        return materials.containsAll(REQUIRED_MATERIALS);
    }
    
    private double calculateClaimAmount(double requestedAmount, double deductible, double ratio) {
        double afterDeductible = Math.max(0, requestedAmount - deductible);
        return afterDeductible * ratio;
    }
    
    private boolean checkDuplicateClaim(String policyId, LocalDateTime accidentTime, String accidentType) {
        List<Claim> claims = dataStore.getAllClaims();
        for (Claim claim : claims) {
            if (claim.getPolicyId().equals(policyId) &&
                claim.getAccidentType().equals(accidentType) &&
                claim.getAccidentTime().toLocalDate().equals(accidentTime.toLocalDate())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkInProgressClaim(String policyId) {
        List<Claim> claims = dataStore.getAllClaims();
        for (Claim claim : claims) {
            if (claim.getPolicyId().equals(policyId) &&
                IN_PROGRESS_STATUSES.contains(claim.getStatus())) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getRequiredMaterials() {
        return REQUIRED_MATERIALS;
    }
}
