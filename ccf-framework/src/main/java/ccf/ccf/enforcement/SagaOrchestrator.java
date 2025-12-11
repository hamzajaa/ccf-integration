package ccf.ccf.enforcement;

import ccf.ccf.exception.SagaExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final CompensationHandler compensationHandler;

    public SagaContext executeSaga(List<SagaStep> steps) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting saga execution: {}", sagaId);

        SagaContext context = new SagaContext();
        context.setSagaId(sagaId);

        List<SagaStep> executedSteps = new ArrayList<>();

        try {
            for (SagaStep step : steps) {
                log.info("Executing saga step: {}", step.getStepName());

                step.getAction().run();
                step.setExecuted(true);
                executedSteps.add(step);

                log.info("Saga step completed: {}", step.getStepName());
            }

            context.setCompleted(true);
            log.info("Saga execution completed successfully: {}", sagaId);

        } catch (Exception e) {
            log.error("Saga execution failed: {}", e.getMessage());
            context.setCompensating(true);

            compensationHandler.compensate(executedSteps);

            throw new SagaExecutionException("Saga execution failed", e);
        }

        return context;
    }

    public void addStep(List<SagaStep> steps, SagaStep step) {
        steps.add(step);
        log.debug("Added saga step: {}", step.getStepName());
    }
}