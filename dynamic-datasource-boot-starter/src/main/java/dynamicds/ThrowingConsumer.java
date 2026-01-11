package dynamicds;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Throwable;
}
