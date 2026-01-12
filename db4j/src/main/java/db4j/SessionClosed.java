package db4j;

/**
 * Thrown when user tries to use a Session/Tx after it is closed.
 */
public class SessionClosed extends RuntimeException {

    public SessionClosed(String message) {
        super(message);
    }
}
