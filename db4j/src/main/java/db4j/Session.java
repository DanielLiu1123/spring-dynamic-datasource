package db4j;

/**
 * Execution session bound to a single datasource.
 */
public interface Session {

    DS ds();

    Access access();

    /**
     * Create a client bound to this session.
     */
    default <C> C client(Class<C> type) {
        return access().client(type);
    }
}
