package ccf.ccf.specification.model;

public enum ConsistencyLevel {
    STRONG,      // ACID, Serializable
    CAUSAL,      // Causal consistency
    EVENTUAL,    // Eventually consistent
    READ_YOUR_WRITES  // Session consistency
}