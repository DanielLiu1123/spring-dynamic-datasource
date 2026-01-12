package db4j;

/**
 * Thrown when cross-datasource access is attempted within a Tx (if enforced).
 */
public class CrossDbAccessInTx extends RuntimeException {

    public CrossDbAccessInTx(String message) {
        super(message);
    }
}
