package ccf.ccf.specification;

import ccf.ccf.specification.model.ConsistencyContract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class ContractRepository {

    private final Map<String, ConsistencyContract> contracts = new HashMap<>();

    public void save(ConsistencyContract contract) {
        log.info("Saving contract: {}", contract.getContractId());
        contracts.put(contract.getContractId(), contract);
    }

    public Optional<ConsistencyContract> findById(String contractId) {
        log.debug("Finding contract by ID: {}", contractId);
        return Optional.ofNullable(contracts.get(contractId));
    }

    public Optional<ConsistencyContract> findByName(String contractName) {
        log.debug("Finding contract by name: {}", contractName);
        return contracts.values().stream()
                .filter(c -> c.getContractName().equals(contractName))
                .findFirst();
    }

    public void delete(String contractId) {
        log.info("Deleting contract: {}", contractId);
        contracts.remove(contractId);
    }

    public Map<String, ConsistencyContract> findAll() {
        return new HashMap<>(contracts);
    }
}