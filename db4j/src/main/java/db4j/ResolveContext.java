package db4j;

/**
 * Context passed to client resolvers during client creation.
 */
public interface ResolveContext {

    DS ds();

    /**
     * Current session; may be null for non-transactional access.
     */
    Session session();

    /**
     * Optional access to underlying resources (may return null).
     */
    <T> T unwrap(Class<T> type);
}
