package ccf.ccf.exception;

public class ConsistencyException extends RuntimeException {

    public ConsistencyException(String message) {
        super(message);
    }

    public ConsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
}