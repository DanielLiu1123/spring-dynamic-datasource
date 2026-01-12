package db4j;

final class TxImpl implements Tx {

    private final DS ds;
    private final TxOptions opts;

    TxImpl(DS ds, TxOptions opts) {
        this.ds = ds;
        this.opts = opts;
    }

    @Override
    public TxOptions options() {
        return opts;
    }

    @Override
    public DS ds() {
        return ds;
    }
}
