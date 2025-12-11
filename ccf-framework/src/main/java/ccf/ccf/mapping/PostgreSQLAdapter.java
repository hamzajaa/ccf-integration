package ccf.ccf.mapping;

import ccf.ccf.specification.model.ConsistencyLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostgreSQLAdapter implements DatabaseAdapter {

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public void applyConsistencyLevel(ConsistencyLevel level) {
        log.info("Applying consistency level {} to PostgreSQL", level);

        String isolationLevel = translateConsistencyLevel(level);
        log.info("Mapped to isolation level: {}", isolationLevel);

        // In real implementation, set transaction isolation level
        // e.g., dataSource.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    @Override
    public String translateConsistencyLevel(ConsistencyLevel level) {
        return switch (level) {
            case STRONG -> "SERIALIZABLE";
            case CAUSAL -> "REPEATABLE READ";
            case EVENTUAL -> "READ COMMITTED";
            case READ_YOUR_WRITES -> "READ COMMITTED";
        };
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }
}