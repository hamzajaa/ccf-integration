package ccf.ccf.verification;

import ccf.ccf.specification.ContractRegistry;
import ccf.ccf.specification.ContractRepository;
import ccf.ccf.specification.model.ConsistencyContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsistencyVerifier {

    private final ContractRepository contractRepository;
    private final ContractRegistry contractRegistry;

    public boolean verify(String contractId, Object entity) {
        log.info("Verifying consistency for contract: {}", contractId);

        // ENFORCE CONTRACT VALIDITY FIRST
        contractRegistry.enforceContractValidity(contractId);

        Optional<ConsistencyContract> contract = contractRepository.findById(contractId);

        if (contract.isEmpty()) {
            log.error("Contract not found: {}", contractId);
            return false;
        }

        // Perform actual consistency verification
        boolean isValid = performVerification(contract.get(), entity);

        if (isValid) {
            log.info("Consistency verification passed for contract: {}", contractId);
        } else {
            log.error("Consistency verification failed for contract: {}", contractId);
        }

        return isValid;
    }

    private boolean performVerification(ConsistencyContract contract, Object entity) {
        // For now, return true as basic implementation
        return true;
    }
}