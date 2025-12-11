package ccf.ccf.specification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRegistrationEvent {
    private String serviceName;
    private String contractId;
    private String contractVersion;
    private String contractHash;
    private long timestamp;
}