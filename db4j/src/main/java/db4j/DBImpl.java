package db4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;

/**
 * Entry point / registry for configured datasources.
 */
final class DBImpl implements DB {

    private final ConcurrentMap<String, DSImpl> datasources = new ConcurrentHashMap<>();

    @Override
    public DS ds(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        var ds = datasources.get(name);
        if (ds == null) throw new IllegalArgumentException("No datasource named " + name);
        return ds;
    }

    @Override
    public void registerDataSource(String name, DataSource dataSource) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(dataSource, "dataSource cannot be null");
        datasources.put(name, new DSImpl(name, dataSource));
    }

    @Override
    public void unregisterDataSource(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        datasources.remove(name);
    }

    void addClientResolver(String dsName, ClientResolver resolver) {
        Objects.requireNonNull(dsName, "dsName cannot be null");
        Objects.requireNonNull(resolver, "resolver cannot be null");
        var ds = datasources.get(dsName);
        if (ds == null) throw new IllegalArgumentException("No datasource named " + dsName);
        ds.addClientResolver(resolver);
    }
}
