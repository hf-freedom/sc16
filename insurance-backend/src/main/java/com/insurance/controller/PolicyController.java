package com.insurance.controller;

import com.insurance.entity.Policy;
import com.insurance.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "http://localhost:3002")
public class PolicyController {
    
    @Autowired
    private PolicyService policyService;
    
    @PostMapping
    public Policy createPolicy(@RequestBody Policy policy) {
        return policyService.createPolicy(policy);
    }
    
    @GetMapping
    public List<Policy> getAllPolicies() {
        return policyService.getAllPolicies();
    }
    
    @GetMapping("/{id}")
    public Policy getPolicy(@PathVariable String id) {
        return policyService.getPolicy(id);
    }
    
    @PutMapping("/{id}/status")
    public Policy updatePolicyStatus(@PathVariable String id, @RequestParam String status) {
        return policyService.updatePolicyStatus(id, status);
    }
}
