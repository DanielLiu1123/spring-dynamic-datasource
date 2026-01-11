package dynamicds;

import java.lang.reflect.UndeclaredThrowableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;

/**
 * This interface is used to switch data sources dynamically.
 *
 * @author Freeman
 * @see <a href="https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">Curiously Recurring Template Pattern</a>
 */
public interface DynamicDataSource<T extends DynamicDataSource<T>> {

    Logger log = LoggerFactory.getLogger(DynamicDataSource.class);

    /**
     * Return a new/cached instance with specified {@link javax.sql.DataSource} bean name.
     *
     * @param dataSource dataSource bean name to use
     * @return new/cached instance with specified {@link javax.sql.DataSource}
     */
    @SuppressWarnings("unchecked")
    default T withDataSource(String dataSource) {
        return (T) resolveContext(dataSource).client();
    }

    private ClientContext resolveContext(Object dataSource) {
        var proxies = ClientProxies.getProxies();
        for (var proxy : proxies) {
            if (proxy.supports(this, dataSource)) {
                var client = proxy.createClient(this, dataSource);
                var tm = proxy.createTransactionManager(dataSource);
                return new ClientContext(client, tm);
            }
        }
        return new ClientContext(this, ClientProxies.getDefaultTransactionManager());
    }

    default void withTransaction(ThrowingConsumer<T> action) {
        withTransaction(TransactionDefinition.withDefaults(), action);
    }

    default void withTransaction(TransactionDefinition definition, ThrowingConsumer<T> action) {
        withTransactionResult(definition, client -> {
            action.accept(client);
            return null;
        });
    }

    default <R> R withTransactionResult(ThrowingFunction<T, R> action) {
        return withTransactionResult(TransactionDefinition.withDefaults(), action);
    }

    default <R> R withTransactionResult(TransactionDefinition definition, ThrowingFunction<T, R> action) {
        var proxies = ClientProxies.getProxies();
        for (var proxy : proxies) {
            var ds = proxy.getDataSource(this);
            if (ds != null) {
                return executeInTransaction(ds, definition, action);
            }
        }
        return executeInTransaction(ClientProxies.getDefaultDataSource(), definition, action);
    }

    @SuppressWarnings("unchecked")
    private <R> R executeInTransaction(
            Object dataSource, TransactionDefinition definition, ThrowingFunction<T, R> action) {
        var context = resolveContext(dataSource);
        var client = (T) context.client();
        var tm = context.transactionManager();
        var status = tm.getTransaction(definition);
        R result;
        try {
            result = action.apply(client);
        } catch (Throwable e) {
            log.error("An exception occurs in the transaction, rolling back", e);
            tm.rollback(status);
            throw wrapRuntimeException(e);
        }
        tm.commit(status);
        return result;
    }

    private static RuntimeException wrapRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new UndeclaredThrowableException(ex);
    }
}
