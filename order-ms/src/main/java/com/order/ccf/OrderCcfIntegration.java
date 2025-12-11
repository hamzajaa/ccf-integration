package com.order.ccf;

import ccf.ccf.specification.ContractParser;
import ccf.ccf.specification.ContractRegistry;
import ccf.ccf.specification.ContractRepository;
import ccf.ccf.specification.ContractValidator;
import ccf.ccf.specification.model.ConsistencyContract;
import ccf.ccf.verification.ConsistencyVerifier;
import com.order.model.Order;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCcfIntegration {

    private final ContractParser contractParser;
    private final ContractValidator contractValidator;
    private final ContractRepository contractRepository;
    private final ContractRegistry contractRegistry;
    private final ConsistencyVerifier consistencyVerifier;

    @PostConstruct
    public void initialize() {
        log.info("Initializing CCF integration for Order Service");
        loadContract();
    }

    private void loadContract() {
        try {
            ConsistencyContract contract = contractParser.parse(
                    "contracts/order-payment-contract.ccf"
            );

            contractValidator.validate(contract);
            contractRepository.save(contract);

            // Register with CCF Registry
            contractRegistry.registerContract("OrderService", contract);

            log.info("Successfully loaded contract: {} version {}",
                    contract.getContractName(), contract.getContractVersion());

        } catch (Exception e) {
            log.error("Failed to load contract: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateOrderConsistency(Order order) {
        log.info("Validating consistency for order: {}", order.getId());

        // This will throw exception if contract is invalid
        return consistencyVerifier.verify("OrderPaymentConsistency", order);
    }
}