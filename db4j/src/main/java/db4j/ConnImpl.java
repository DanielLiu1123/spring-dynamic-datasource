package db4j;

import java.sql.Connection;

final class ConnImpl implements Conn {

    private final DSImpl ds;
    private final Connection connection;

    ConnImpl(DSImpl ds, Connection connection) {
        this.ds = ds;
        this.connection = connection;
    }

    @Override
    public <C> C client(Class<C> type) {
        return ds.client(connection, type);
    }
}
