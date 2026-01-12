package db4j;

@FunctionalInterface
public interface ConnCallback<T> {
    T apply(Conn conn) throws Exception;
}
