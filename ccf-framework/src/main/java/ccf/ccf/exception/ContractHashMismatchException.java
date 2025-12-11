package ccf.ccf.exception;

public class ContractHashMismatchException extends ConsistencyException {
    public ContractHashMismatchException(String message) {
        super(message);
    }
}