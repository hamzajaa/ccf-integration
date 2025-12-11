package ccf.ccf.specification;

import ccf.ccf.exception.ContractVersionMismatchException;
import ccf.ccf.specification.model.ConsistencyContract;
import ccf.ccf.specification.model.ContractRegistrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractRegistry {

    private final Map<String, ContractRegistration> registrations = new ConcurrentHashMap<>();
    private final Map<String, Boolean> contractValidityStatus = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String CONTRACT_TOPIC = "contract-registrations";

    public void registerContract(String serviceName, ConsistencyContract contract) {
        String contractId = contract.getContractId();

        log.info("Registering contract {} for service {}", contractId, serviceName);

        // Publish registration event to Kafka
        ContractRegistrationEvent event = ContractRegistrationEvent.builder()
                .serviceName(serviceName)
                .contractId(contractId)
                .contractVersion(contract.getContractVersion())
                .contractHash(contract.getContractHash())
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(CONTRACT_TOPIC, contractId, event);

        // Also register locally
        registerLocally(serviceName, contract);
    }

    @KafkaListener(topics = CONTRACT_TOPIC, groupId = "ccf-registry-group")
    public void handleContractRegistration(String message) {
        try {
            ContractRegistrationEvent event = objectMapper.readValue(message, ContractRegistrationEvent.class);

            String contractId = event.getContractId();
            String serviceName = event.getServiceName();

            log.info("Received contract registration from {}: {} v{}",
                    serviceName, contractId, event.getContractVersion());

            boolean isValid = true;

            if (registrations.containsKey(contractId)) {
                ContractRegistration existing = registrations.get(contractId);

                // Validate version
                if (!event.getContractVersion().equals(existing.getVersion())) {
                    String error = String.format(
                            "⚠️ CONTRACT VERSION MISMATCH! Service %s uses v%s but other services use v%s for contract %s",
                            serviceName, event.getContractVersion(), existing.getVersion(), contractId
                    );
                    log.error(error);
                    isValid = false;
                }

                // Validate hash
                if (!event.getContractHash().equals(existing.getHash())) {
                    String error = String.format(
                            "⚠️ CONTRACT HASH MISMATCH! Service %s has different contract content for %s",
                            serviceName, contractId
                    );
                    log.error(error);
                    isValid = false;
                }
            }

            // Update validity status
            contractValidityStatus.put(contractId, isValid);

            // Update registration
            registrations.computeIfAbsent(contractId,
                            k -> new ContractRegistration(event.getContractVersion(), event.getContractHash()))
                    .addService(serviceName);

            if (isValid) {
                log.info("✅ Contract {} now registered by services: {}",
                        contractId, registrations.get(contractId).getServices());
            } else {
                log.error("❌ Contract {} is INVALID - operations will be BLOCKED!", contractId);
            }

        } catch (Exception e) {
            log.error("Error processing contract registration: {}", e.getMessage(), e);
        }
    }

    private void registerLocally(String serviceName, ConsistencyContract contract) {
        String contractId = contract.getContractId();

        registrations.computeIfAbsent(contractId,
                        k -> new ContractRegistration(contract.getContractVersion(), contract.getContractHash()))
                .addService(serviceName);

        // Initially mark as valid (will be updated when other services register)
        contractValidityStatus.putIfAbsent(contractId, true);
    }

    public boolean isContractConsistent(String contractId) {
        ContractRegistration registration = registrations.get(contractId);
        return registration != null && registration.getServices().size() >= 2;
    }

    public boolean isContractValid(String contractId) {
        return contractValidityStatus.getOrDefault(contractId, true);
    }

    public void enforceContractValidity(String contractId) {
        if (!isContractValid(contractId)) {
            String error = String.format(
                    "Contract %s is INVALID due to version or content mismatch between services. Operation BLOCKED!",
                    contractId
            );
            log.error(error);
            throw new ContractVersionMismatchException(error);
        }
    }

    public ContractRegistration getRegistration(String contractId) {
        return registrations.get(contractId);
    }

    @lombok.Data
    public static class ContractRegistration {
        private final String version;
        private final String hash;
        private final Set<String> services = ConcurrentHashMap.newKeySet();

        public ContractRegistration(String version, String hash) {
            this.version = version;
            this.hash = hash;
        }

        public void addService(String serviceName) {
            services.add(serviceName);
        }
    }
}