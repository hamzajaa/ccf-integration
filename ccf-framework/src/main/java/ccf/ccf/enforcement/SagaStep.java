package ccf.ccf.enforcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {
    private String stepId;
    private String stepName;
    private String serviceName;
    private Runnable action;
    private Runnable compensation;
    private int timeoutSeconds;
    private boolean executed;
    private boolean compensated;
}