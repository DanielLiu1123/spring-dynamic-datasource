package db4j;

@FunctionalInterface
public interface SessionCallback<T> {
    T apply(Session session) throws Exception;
}
