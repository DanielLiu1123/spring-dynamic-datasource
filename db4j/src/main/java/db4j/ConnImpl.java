package db4j;

import java.sql.Connection;

final class ConnImpl implements Conn {

    private final DS ds;
    private final Connection conn;

    ConnImpl(DS ds, Connection conn) {
        this.ds = ds;
        this.conn = conn;
    }

    @Override
    public DS ds() {
        return ds;
    }
}
