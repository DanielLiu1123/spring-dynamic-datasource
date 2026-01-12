package db4j;

/**
 * Transaction is a Session + transactional metadata.
 * Tx MUST still be bound to one DS (no implicit cross-datasource).
 */
public interface Tx extends Conn {

    /**
     * Options used to create this tx.
     */
    TxOptions options();
}
