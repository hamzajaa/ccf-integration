package ccf.ccf.enforcement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CompensationHandler {

    public void compensate(List<SagaStep> executedSteps) {
        log.warn("Starting compensation for {} executed steps", executedSteps.size());

        // Compensate in reverse order
        Collections.reverse(executedSteps);

        for (SagaStep step : executedSteps) {
            if (step.isExecuted() && !step.isCompensated()) {
                try {
                    log.info("Compensating step: {}", step.getStepName());

                    if (step.getCompensation() != null) {
                        step.getCompensation().run();
                        step.setCompensated(true);

                        log.info("Compensation completed for step: {}", step.getStepName());
                    } else {
                        log.warn("No compensation defined for step: {}", step.getStepName());
                    }

                } catch (Exception e) {
                    log.error("Compensation failed for step {}: {}", step.getStepName(), e.getMessage());
                    // Continue with other compensations
                }
            }
        }

        log.info("Compensation process completed");
    }
}