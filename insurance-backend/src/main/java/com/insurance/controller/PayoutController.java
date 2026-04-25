package com.insurance.controller;

import com.insurance.entity.Payout;
import com.insurance.service.PayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payouts")
@CrossOrigin(origins = "http://localhost:3002")
public class PayoutController {
    
    @Autowired
    private PayoutService payoutService;
    
    @PostMapping
    public ResponseEntity<?> createPayout(@RequestBody Map<String, String> request) {
        try {
            String claimId = request.get("claimId");
            Payout payout = payoutService.createPayout(claimId);
            return ResponseEntity.ok(payout);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completePayout(@PathVariable String id) {
        try {
            Payout payout = payoutService.completePayout(id);
            return ResponseEntity.ok(payout);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    public List<Payout> getAllPayouts() {
        return payoutService.getAllPayouts();
    }
    
    @GetMapping("/{id}")
    public Payout getPayout(@PathVariable String id) {
        return payoutService.getPayout(id);
    }
}
