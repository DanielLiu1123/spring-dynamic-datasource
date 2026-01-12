package db4j;

import java.sql.Connection;

final class TxImpl implements Tx {

    private final DSImpl ds;
    private final Connection connection;
    private final TxOptions opts;

    TxImpl(DSImpl ds, Connection connection, TxOptions opts) {
        this.ds = ds;
        this.connection = connection;
        this.opts = opts;
    }

    //    @Override
    //    public TxOptions options() {
    //        return opts;
    //    }

    @Override
    public <C> C client(Class<C> type) {
        return ds.client(connection, type);
    }
}
