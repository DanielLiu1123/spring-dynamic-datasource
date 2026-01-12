package db4j;

/**
 * Stateless access factory for creating clients.
 */
public interface Access {

    <C> C client(Class<C> type);
}
