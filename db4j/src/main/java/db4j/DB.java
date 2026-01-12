package db4j;

import javax.sql.DataSource;

/**
 * Entry point / registry for configured datasources.
 */
public interface DB {

    /**
     * Get a strong handle to a configured datasource by name.
     */
    DS ds(String name);

    /**
     * Register a new datasource by name.
     */
    void registerDataSource(String name, DataSource dataSource);

    /**
     * Remove a registered datasource by name.
     */
    void unregisterDataSource(String name);

    /**
     * Create a new DB instance.
     *
     * @return new DB instance
     */
    static DB create() {
        return new DBImpl();
    }
}
