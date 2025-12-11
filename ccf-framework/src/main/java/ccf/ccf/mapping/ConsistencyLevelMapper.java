package ccf.ccf.mapping;

import ccf.ccf.specification.model.ConsistencyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsistencyLevelMapper {

    private final PostgreSQLAdapter postgreSQLAdapter;
    private final MongoDBAdapter mongoDBAdapter;

    private final Map<String, DatabaseAdapter> adapters = new HashMap<>();

    public void initialize() {
        adapters.put("PostgreSQL", postgreSQLAdapter);
        adapters.put("MongoDB", mongoDBAdapter);
    }

    public void applyConsistencyLevel(String databaseType, ConsistencyLevel level) {
        log.info("Applying consistency level {} for database type {}", level, databaseType);

        DatabaseAdapter adapter = adapters.get(databaseType);
        if (adapter == null) {
            log.warn("No adapter found for database type: {}", databaseType);
            throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }

        adapter.applyConsistencyLevel(level);
    }

    public String getTranslatedLevel(String databaseType, ConsistencyLevel level) {
        DatabaseAdapter adapter = adapters.get(databaseType);
        if (adapter == null) {
            return "UNKNOWN";
        }
        return adapter.translateConsistencyLevel(level);
    }
}