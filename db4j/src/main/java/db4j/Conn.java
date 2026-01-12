package db4j;

/**
 * A concrete connection bound to exactly one DbRef.
 * Mappers created from the same Conn share the same underlying connection.
 */
public interface Conn {
    /**
     * The DS this Conn is bound to.
     */
    DS ds();

    /**
     * Create a client bound to THIS connection.
     */
    <C> C client(Class<C> clientType);
}
