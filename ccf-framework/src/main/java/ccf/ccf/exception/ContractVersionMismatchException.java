package ccf.ccf.exception;

public class ContractVersionMismatchException extends ConsistencyException {
    public ContractVersionMismatchException(String message) {
        super(message);
    }
}