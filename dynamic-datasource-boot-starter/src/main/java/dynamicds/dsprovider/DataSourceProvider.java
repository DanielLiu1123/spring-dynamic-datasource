package dynamicds.dsprovider;

import dynamicds.DataSourcesProperties;
import javax.sql.DataSource;

/**
 * Factory for creating DataSource instances.
 */
public interface DataSourceProvider {

    /**
     * Returns the DataSource type supported by this provider.
     *
     * @return the DataSource type
     */
    Class<? extends DataSource> getType();

    /**
     * Creates a DataSource based on the provided configuration.
     *
     * @param properties the data source properties
     * @return a new DataSource instance
     */
    DataSource createDataSource(DataSourcesProperties.DataSource properties);
}
