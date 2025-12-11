package ccf.ccf.verification;

import ccf.ccf.verification.model.ConsistencyViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ViolationDetector {

    private final List<ConsistencyViolation> detectedViolations = new ArrayList<>();

    public void detectViolation(String contractId, String invariant, String serviceName, Object context) {
        log.warn("Consistency violation detected in contract: {}", contractId);

        ConsistencyViolation violation = ConsistencyViolation.builder()
                .violationId(UUID.randomUUID().toString())
                .contractId(contractId)
                .invariant(invariant)
                .description("Invariant violation: " + invariant)
                .timestamp(LocalDateTime.now())
                .serviceName(serviceName)
                .context(context)
                .build();

        detectedViolations.add(violation);

        log.error("Violation recorded: {}", violation.getViolationId());

        // Trigger alerts
        triggerAlert(violation);
    }

    private void triggerAlert(ConsistencyViolation violation) {
        log.error("ALERT: Consistency violation in service {} - {}",
                violation.getServiceName(), violation.getDescription());

        // In real implementation, send to monitoring system, email, etc.
    }

    public List<ConsistencyViolation> getViolations() {
        return new ArrayList<>(detectedViolations);
    }

    public void clearViolations() {
        detectedViolations.clear();
    }
}