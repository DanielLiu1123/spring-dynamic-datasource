package db4j;

/**
 * Thrown when user tries to use a Conn/Tx after it is closed.
 */
public class ConnectionClosed extends RuntimeException {

    public ConnectionClosed(String message) {
        super(message);
    }
}
