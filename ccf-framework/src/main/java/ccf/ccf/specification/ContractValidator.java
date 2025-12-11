package ccf.ccf.specification;

import ccf.ccf.exception.ContractViolationException;
import ccf.ccf.specification.model.ConsistencyContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ContractValidator {

    public void validate(ConsistencyContract contract) {
        log.info("Validating contract: {}", contract.getContractName());

        List<String> errors = new ArrayList<>();

        // Validate contract name
        if (contract.getContractName() == null || contract.getContractName().isEmpty()) {
            errors.add("Contract name is required");
        }

        // Validate services
        if (contract.getServices() == null || contract.getServices().isEmpty()) {
            errors.add("At least one service must be specified");
        }

        // Validate consistency level
        if (contract.getConsistencyLevel() == null) {
            errors.add("Consistency level is required");
        }

        // Validate invariants
        if (contract.getInvariants() == null || contract.getInvariants().isEmpty()) {
            errors.add("At least one invariant must be defined");
        }

        if (!errors.isEmpty()) {
            log.error("Contract validation failed: {}", errors);
            throw new ContractViolationException("Contract validation failed: " + errors);
        }

        log.info("Contract validation successful");
    }

    public boolean checkInvariant(String invariant, Object context) {
        log.debug("Checking invariant: {}", invariant);
        // Simplified invariant checking
        // In real implementation, evaluate the invariant expression against context
        return true;
    }
}