package com.insurance.service;

import com.insurance.entity.Policy;
import com.insurance.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyService {
    
    private final DataStore dataStore = DataStore.getInstance();
    
    public Policy createPolicy(Policy policy) {
        if (policy.getRemainingAmount() == null) {
            policy.setRemainingAmount(policy.getTotalAmount());
        }
        if (policy.getStatus() == null) {
            policy.setStatus("ACTIVE");
        }
        return dataStore.savePolicy(policy);
    }
    
    public Policy getPolicy(String id) {
        return dataStore.getPolicy(id);
    }
    
    public List<Policy> getAllPolicies() {
        return dataStore.getAllPolicies();
    }
    
    public Policy updatePolicyStatus(String id, String status) {
        Policy policy = dataStore.getPolicy(id);
        if (policy != null) {
            policy.setStatus(status);
            dataStore.savePolicy(policy);
        }
        return policy;
    }
    
    public Policy reduceRemainingAmount(String id, Double amount) {
        Policy policy = dataStore.getPolicy(id);
        if (policy != null && policy.getRemainingAmount() != null) {
            double newRemaining = policy.getRemainingAmount() - amount;
            policy.setRemainingAmount(Math.max(0, newRemaining));
            dataStore.savePolicy(policy);
        }
        return policy;
    }
}
