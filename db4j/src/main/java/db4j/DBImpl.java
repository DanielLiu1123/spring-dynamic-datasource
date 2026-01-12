package db4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;

/**
 * Entry point / registry for configured datasources.
 */
final class DBImpl implements DB {

    private final ConcurrentMap<String, DS> datasources = new ConcurrentHashMap<>();

    @Override
    public DS ds(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        var ds = datasources.get(name);
        if (ds == null) throw new IllegalArgumentException("No datasource named " + name);
        return ds;
    }

    @Override
    public void register(String name, DataSource dataSource) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(dataSource, "dataSource cannot be null");
        datasources.put(name, new DSImpl(name, dataSource));
    }

    @Override
    public void unregister(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        datasources.remove(name);
    }
}
