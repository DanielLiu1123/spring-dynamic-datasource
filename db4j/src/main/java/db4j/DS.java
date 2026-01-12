package db4j;

/**
 *
 */
public interface DS {

    /**
     * Stable logical name of this datasource (e.g. "main").
     */
    String name();

    /**
     * Obtain a session and run a callback (no transaction).
     */
    <T> T withSession(SessionCallback<T> fn);

    /**
     * Execute within a transaction; auto commit on success, rollback on exception.
     */
    <T> T inTx(TxOptions opts, TxCallback<T> fn);

    default <T> T inTx(TxCallback<T> fn) {
        return inTx(TxOptions.defaults(), fn);
    }

    /**
     * Stateless access factory for this datasource.
     */
    Access access();

    /**
     * Non-transactional client (each call may use its own connection).
     */
    default <C> C client(Class<C> clientType) {
        return access().client(clientType);
    }
}
