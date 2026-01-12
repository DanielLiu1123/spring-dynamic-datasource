package db4j;

/**
 *
 */
public interface DS {

    /**
     * Obtain a connection and run a callback (no transaction).
     */
    <T> T conn(ConnCallback<T> fn);

    /**
     * Execute within a transaction; auto commit on success, rollback on exception.
     */
    <T> T tx(TxOptions opts, TxCallback<T> fn);

    default <T> T tx(TxCallback<T> fn) {
        return tx(TxOptions.defaults(), fn);
    }

    /**
     * Non-transactional client (each call may use its own connection).
     */
    <C> C client(Class<C> clientType);
}
