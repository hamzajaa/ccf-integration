package ccf.ccf.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsistencyViolation {
    private String violationId;
    private String contractId;
    private String invariant;
    private String description;
    private LocalDateTime timestamp;
    private String serviceName;
    private Object context;
}