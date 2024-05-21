package Ignite;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    // Constructors
    // ------------------------------------------------------------------------

    public ValidationException(String messageCode) {
        super(messageCode);
    }

    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
