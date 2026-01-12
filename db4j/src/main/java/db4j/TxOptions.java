package db4j;

import java.time.Duration;

/**
 * Transaction options.
 */
public record TxOptions(Isolation isolation, boolean readOnly, Duration timeout) {

    public static TxOptions defaults() {
        return new TxOptions(Isolation.UNSPECIFIED, false, Duration.ofSeconds(30));
    }

    public TxOptions withIsolation(Isolation v) {
        return new TxOptions(v, readOnly, timeout);
    }

    public TxOptions withReadOnly(boolean v) {
        return new TxOptions(isolation, v, timeout);
    }

    public TxOptions withTimeout(Duration v) {
        return new TxOptions(isolation, readOnly, v);
    }
}
