package com.insurance.config;

import com.insurance.entity.Customer;
import com.insurance.entity.Policy;
import com.insurance.service.PolicyService;
import com.insurance.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private PolicyService policyService;
    
    private final DataStore dataStore = DataStore.getInstance();
    
    @Override
    public void run(String... args) {
        Customer customer1 = Customer.builder()
            .name("张三")
            .idCard("110101199001011234")
            .riskLevel("LOW")
            .build();
        dataStore.saveCustomer(customer1);
        
        Customer customer2 = Customer.builder()
            .name("李四")
            .idCard("310101198505054321")
            .riskLevel("MEDIUM")
            .build();
        dataStore.saveCustomer(customer2);
        
        Policy policy1 = Policy.builder()
            .customerId(customer1.getId())
            .insuranceType("意外险")
            .totalAmount(100000.0)
            .remainingAmount(100000.0)
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .deductible(500.0)
            .payoutRatio(0.8)
            .status("ACTIVE")
            .coverageTypes(Arrays.asList("意外身故", "意外伤残", "意外医疗"))
            .build();
        policyService.createPolicy(policy1);
        
        Policy policy2 = Policy.builder()
            .customerId(customer2.getId())
            .insuranceType("重疾险")
            .totalAmount(300000.0)
            .remainingAmount(300000.0)
            .startDate(LocalDate.of(2025, 6, 1))
            .endDate(LocalDate.of(2027, 5, 31))
            .deductible(0.0)
            .payoutRatio(1.0)
            .status("ACTIVE")
            .coverageTypes(Arrays.asList("恶性肿瘤", "急性心肌梗塞", "脑中风后遗症"))
            .build();
        policyService.createPolicy(policy2);
        
        Policy policy3 = Policy.builder()
            .customerId(customer1.getId())
            .insuranceType("医疗险")
            .totalAmount(50000.0)
            .remainingAmount(50000.0)
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .deductible(1000.0)
            .payoutRatio(0.7)
            .status("ACTIVE")
            .coverageTypes(Arrays.asList("住院医疗", "门诊医疗", "手术医疗"))
            .build();
        policyService.createPolicy(policy3);
    }
}
