package dynamicds.dsprovider;

import dynamicds.DataSourcesProperties;
import javax.sql.DataSource;

/**
 * Factory for creating DataSource instances.
 */
public interface DataSourceProvider {

    /**
     * Checks if this provider supports the given DataSource type.
     *
     * @param type the DataSource type
     * @return true if this provider supports the given DataSource type
     */
    boolean supports(Class<? extends DataSource> type);

    /**
     * Creates a DataSource based on the provided configuration.
     *
     * @param properties the data source properties
     * @return a new DataSource instance
     */
    DataSource createDataSource(DataSourcesProperties.DataSource properties);
}
