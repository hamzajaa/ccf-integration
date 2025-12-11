package ccf.ccf.enforcement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCoordinator {

    private final SagaOrchestrator sagaOrchestrator;

    public void coordinateTransaction(String transactionId, List<SagaStep> steps) {
        log.info("Coordinating transaction: {}", transactionId);

        try {
            SagaContext context = sagaOrchestrator.executeSaga(steps);

            if (context.isCompleted()) {
                log.info("Transaction completed successfully: {}", transactionId);
            } else {
                log.warn("Transaction not completed: {}", transactionId);
            }

        } catch (Exception e) {
            log.error("Transaction coordination failed: {}", e.getMessage());
            throw e;
        }
    }

    public List<SagaStep> buildSteps() {
        return new ArrayList<>();
    }
}