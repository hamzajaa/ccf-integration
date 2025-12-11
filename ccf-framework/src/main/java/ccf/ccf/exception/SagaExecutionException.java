package ccf.ccf.exception;

public class SagaExecutionException extends ConsistencyException {

    public SagaExecutionException(String message) {
        super(message);
    }

    public SagaExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}