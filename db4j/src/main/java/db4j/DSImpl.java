package db4j;

import javax.sql.DataSource;

/**
 * A handle bound to one datasource (aka "DB").
 */
final class DSImpl implements DS {

    private final String name;
    private final DataSource dataSource;

    DSImpl(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    @Override
    public <T> T conn(ConnCallback<T> fn) {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T tx(TxOptions opts, TxCallback<T> fn) {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> C client(Class<C> clientType) {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }
}
