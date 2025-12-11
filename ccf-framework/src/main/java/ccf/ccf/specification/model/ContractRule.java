package ccf.ccf.specification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRule {
    private String ruleId;
    private String description;
    private String condition;
    private String action;
}