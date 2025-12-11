package ccf.ccf.mapping;

import ccf.ccf.specification.model.ConsistencyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoDBAdapter implements DatabaseAdapter {

    @Override
    public String getDatabaseType() {
        return "MongoDB";
    }

    @Override
    public void applyConsistencyLevel(ConsistencyLevel level) {
        log.info("Applying consistency level {} to MongoDB", level);

        String writeConcern = translateConsistencyLevel(level);
        log.info("Mapped to write concern: {}", writeConcern);

        // In real implementation, set MongoDB write concern
        // e.g., WriteConcern.MAJORITY
    }

    @Override
    public String translateConsistencyLevel(ConsistencyLevel level) {
        return switch (level) {
            case STRONG -> "MAJORITY with Read Concern LINEARIZABLE";
            case CAUSAL -> "MAJORITY with Causal Consistency";
            case EVENTUAL -> "W:1";
            case READ_YOUR_WRITES -> "MAJORITY";
        };
    }

    @Override
    public boolean supportsTransactions() {
        return true; // MongoDB 4.0+
    }
}