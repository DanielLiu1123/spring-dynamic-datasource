package dynamicds;

import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Strategy interface for creating and managing dynamic clients.
 */
public interface ClientProxy {

    /**
     * Checks if this proxy supports the given client and data source.
     *
     * @param client     the client instance
     * @param dataSource the data source identifier (bean name or DataSource instance)
     * @return true if supported
     */
    boolean supports(Object client, Object dataSource);

    /**
     * Creates a transaction manager for the specified data source.
     *
     * @param dataSource the data source identifier
     * @return a new or cached transaction manager
     */
    PlatformTransactionManager createTransactionManager(Object dataSource);

    /**
     * Creates a new client instance associated with the specified data source.
     *
     * @param client     the original client instance
     * @param dataSource the data source identifier
     * @param <T>        the client type
     * @return the proxied client
     */
    <T> T createClient(T client, Object dataSource);

    /**
     * Retrieves the data source currently associated with the given client.
     *
     * @param client the client instance
     * @return the associated data source, or null if not managed by this proxy
     */
    @Nullable
    DataSource getDataSource(Object client);
}
