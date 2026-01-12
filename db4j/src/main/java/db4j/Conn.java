package db4j;

/**
 * Execution session bound to a single datasource.
 */
public interface Conn {

    DS ds();

    /**
     * Create a client bound to this session.
     */
    default <C> C client(Class<C> type) {
        return ds().client(type);
    }
}
