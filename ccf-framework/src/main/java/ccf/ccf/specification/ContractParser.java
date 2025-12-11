package ccf.ccf.specification;

import ccf.ccf.specification.model.ConsistencyContract;
import ccf.ccf.specification.model.ConsistencyLevel;
import ccf.ccf.specification.model.ContractRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ContractParser {

    private final ResourceLoader resourceLoader;

    public ContractParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ConsistencyContract parse(String contractFilePath) {
        log.info("Parsing contract from file: {}", contractFilePath);

        try {
            String content;

            // Try to load from classpath
            Resource resource = resourceLoader.getResource("classpath:" + contractFilePath);
            if (resource.exists()) {
                content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.info("Loaded contract from classpath: {}", contractFilePath);
            } else {
                throw new IOException("Contract file not found: " + contractFilePath);
            }

            return parseContent(content);
        } catch (IOException e) {
            log.error("Error reading contract file: {}", contractFilePath, e);
            throw new RuntimeException("Failed to parse contract", e);
        }
    }

    private ConsistencyContract parseContent(String content) {
        ConsistencyContract contract = new ConsistencyContract();

        // Parse contract name
        contract.setContractName(extractValue(content, "CONTRACT\\s+(\\w+)"));
        contract.setContractId(contract.getContractName());

        // Parse version
        String version = extractValue(content, "VERSION:\\s*([\\d\\.]+)");
        contract.setContractVersion(version.isEmpty() ? "1.0.0" : version);

        // Parse services
        contract.setServices(extractList(content, "SERVICES:\\s*\\[([^\\]]+)\\]"));

        // Parse consistency level
        String level = extractValue(content, "CONSISTENCY_LEVEL:\\s*(\\w+)");
        contract.setConsistencyLevel(ConsistencyLevel.valueOf(level));

        // Parse invariants
        contract.setInvariants(extractInvariants(content));

        // Parse rules
        contract.setRules(extractRules(content));

        // Parse saga definition
        contract.setSagaDefinition(extractSagaDefinition(content));

        // Generate hash for validation
        contract.setContractHash(generateContractHash(contract));

        log.info("Successfully parsed contract: {} version {}",
                contract.getContractName(), contract.getContractVersion());
        return contract;
    }

    private String generateContractHash(ConsistencyContract contract) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String contractData = contract.getContractName() +
                    contract.getContractVersion() +
                    contract.getServices() +
                    contract.getConsistencyLevel() +
                    contract.getInvariants() +
                    contract.getSagaDefinition();
            byte[] hash = digest.digest(contractData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate contract hash", e);
            return "";
        }
    }

    private String extractValue(String content, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private List<String> extractList(String content, String regex) {
        String value = extractValue(content, regex);
        List<String> result = new ArrayList<>();
        if (!value.isEmpty()) {
            String[] items = value.split(",");
            for (String item : items) {
                result.add(item.trim());
            }
        }
        return result;
    }

    private List<String> extractInvariants(String content) {
        List<String> invariants = new ArrayList<>();
        Pattern pattern = Pattern.compile("INVARIANTS:\\s*\\{([^}]+)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String invariantBlock = matcher.group(1);
            String[] lines = invariantBlock.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("-")) {
                    invariants.add(line.substring(1).trim());
                }
            }
        }
        return invariants;
    }

    private List<ContractRule> extractRules(String content) {
        List<ContractRule> rules = new ArrayList<>();
        // Simplified rule extraction
        // In real implementation, use ANTLR4 for complex parsing
        return rules;
    }

    private String extractSagaDefinition(String content) {
        Pattern pattern = Pattern.compile("SAGA:\\s*\\{([^}]+)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}