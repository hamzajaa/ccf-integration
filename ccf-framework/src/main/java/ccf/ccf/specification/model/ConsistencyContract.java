package ccf.ccf.specification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsistencyContract {
    private String contractId;
    private String contractName;
    private String contractVersion;
    private String contractHash;
    private List<String> services;
    private ConsistencyLevel consistencyLevel;
    private List<ContractRule> rules;
    private List<String> invariants;
    private String sagaDefinition;
}