package db4j;

/**
 * Execution session bound to a single datasource.
 */
public interface Conn {

    /**
     * Create a client bound to this session.
     */
    <C> C client(Class<C> type);
}
