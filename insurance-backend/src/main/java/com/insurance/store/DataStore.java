package com.insurance.store;

import com.insurance.entity.Claim;
import com.insurance.entity.Customer;
import com.insurance.entity.Payout;
import com.insurance.entity.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Policy> policies = new ConcurrentHashMap<>();
    private final Map<String, Claim> claims = new ConcurrentHashMap<>();
    private final Map<String, Payout> payouts = new ConcurrentHashMap<>();
    
    private final AtomicLong customerIdCounter = new AtomicLong(1);
    private final AtomicLong policyIdCounter = new AtomicLong(1);
    private final AtomicLong claimIdCounter = new AtomicLong(1);
    private final AtomicLong payoutIdCounter = new AtomicLong(1);
    
    private DataStore() {}
    
    public static DataStore getInstance() {
        return INSTANCE;
    }
    
    public String generateCustomerId() {
        return "C" + customerIdCounter.getAndIncrement();
    }
    
    public String generatePolicyId() {
        return "P" + policyIdCounter.getAndIncrement();
    }
    
    public String generateClaimId() {
        return "CL" + claimIdCounter.getAndIncrement();
    }
    
    public String generatePayoutId() {
        return "PA" + payoutIdCounter.getAndIncrement();
    }
    
    public Customer saveCustomer(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(generateCustomerId());
        }
        customers.put(customer.getId(), customer);
        return customer;
    }
    
    public Customer getCustomer(String id) {
        return customers.get(id);
    }
    
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }
    
    public Policy savePolicy(Policy policy) {
        if (policy.getId() == null) {
            policy.setId(generatePolicyId());
        }
        policies.put(policy.getId(), policy);
        return policy;
    }
    
    public Policy getPolicy(String id) {
        return policies.get(id);
    }
    
    public List<Policy> getAllPolicies() {
        return new ArrayList<>(policies.values());
    }
    
    public Claim saveClaim(Claim claim) {
        if (claim.getId() == null) {
            claim.setId(generateClaimId());
        }
        claims.put(claim.getId(), claim);
        return claim;
    }
    
    public Claim getClaim(String id) {
        return claims.get(id);
    }
    
    public List<Claim> getAllClaims() {
        return new ArrayList<>(claims.values());
    }
    
    public Payout savePayout(Payout payout) {
        if (payout.getId() == null) {
            payout.setId(generatePayoutId());
        }
        payouts.put(payout.getId(), payout);
        return payout;
    }
    
    public Payout getPayout(String id) {
        return payouts.get(id);
    }
    
    public List<Payout> getAllPayouts() {
        return new ArrayList<>(payouts.values());
    }
}
