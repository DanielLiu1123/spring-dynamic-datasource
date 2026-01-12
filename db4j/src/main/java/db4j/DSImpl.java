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
    public String name() {
        return name;
    }

    @Override
    public <T> T withSession(SessionCallback<T> fn) {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T inTx(TxOptions opts, TxCallback<T> fn) {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }

    @Override
    public Access access() {
        // Not implemented yet
        throw new UnsupportedOperationException();
    }
}
