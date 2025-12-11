package com.payment.ccf;

import ccf.ccf.specification.ContractParser;
import ccf.ccf.specification.ContractRegistry;
import ccf.ccf.specification.ContractRepository;
import ccf.ccf.specification.ContractValidator;
import ccf.ccf.specification.model.ConsistencyContract;
import ccf.ccf.verification.ConsistencyVerifier;
import com.payment.model.Payment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCcfIntegration {

    private final ContractParser contractParser;
    private final ContractValidator contractValidator;
    private final ContractRepository contractRepository;
    private final ContractRegistry contractRegistry;
    private final ConsistencyVerifier consistencyVerifier;

    @PostConstruct
    public void initialize() {
        log.info("Initializing CCF integration for Payment Service");
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
            contractRegistry.registerContract("PaymentService", contract);

            log.info("Successfully loaded contract: {} version {}",
                    contract.getContractName(), contract.getContractVersion());

        } catch (Exception e) {
            log.error("Failed to load contract: {}", e.getMessage());
            throw e;  // Fail fast if contract is invalid
        }
    }

    public boolean validatePaymentConsistency(Payment payment) {
        log.info("Validating consistency for payment: {}", payment.getId());
        return consistencyVerifier.verify("OrderPaymentConsistency", payment);
    }
}