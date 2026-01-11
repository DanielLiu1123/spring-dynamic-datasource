package dynamicds;

import javax.sql.DataSource;

/**
 * Factory for creating DataSource instances.
 */
public interface DataSourceProvider {

    /**
     * Creates a DataSource based on the provided configuration.
     *
     * @param properties the data source properties
     * @return a new DataSource instance
     */
    DataSource createDataSource(DataSourcesProperties.DataSource properties);
}
