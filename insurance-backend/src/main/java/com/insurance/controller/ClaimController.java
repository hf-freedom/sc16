package com.insurance.controller;

import com.insurance.entity.Claim;
import com.insurance.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:3002")
public class ClaimController {
    
    @Autowired
    private ClaimService claimService;
    
    @PostMapping
    public ResponseEntity<?> submitClaim(@RequestBody Claim claim) {
        try {
            Claim saved = claimService.submitClaim(claim);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    public List<Claim> getAllClaims() {
        return claimService.getAllClaims();
    }
    
    @GetMapping("/{id}")
    public Claim getClaim(@PathVariable String id) {
        return claimService.getClaim(id);
    }
    
    @PostMapping("/{id}/materials")
    public ResponseEntity<?> addMaterials(@PathVariable String id, @RequestBody List<String> materials) {
        try {
            Claim updated = claimService.addMaterials(id, materials);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewClaim(
            @PathVariable String id,
            @RequestBody Map<String, Object> reviewData) {
        try {
            Boolean approved = (Boolean) reviewData.get("approved");
            String comment = (String) reviewData.get("comment");
            if (comment == null) comment = "";
            Claim updated = claimService.reviewClaim(id, approved, comment);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/required-materials")
    public List<String> getRequiredMaterials() {
        return claimService.getRequiredMaterials();
    }
}
