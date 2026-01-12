package db4j;

@FunctionalInterface
public interface TxCallback<T> {
    T apply(Tx tx) throws Exception;
}
