package ccf.ccf.mapping;

import ccf.ccf.specification.model.ConsistencyLevel;

public interface DatabaseAdapter {

    String getDatabaseType();

    void applyConsistencyLevel(ConsistencyLevel level);

    String translateConsistencyLevel(ConsistencyLevel level);

    boolean supportsTransactions();
}