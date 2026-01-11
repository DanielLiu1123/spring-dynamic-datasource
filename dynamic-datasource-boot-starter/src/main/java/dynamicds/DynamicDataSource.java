package dynamicds;

import java.lang.reflect.UndeclaredThrowableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
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
        return (T) withDataSource0(dataSource).client();
    }

    private T2 withDataSource0(Object dataSource) {
        var proxies = ClientProxies.getProxies();
        for (var proxy : proxies) {
            if (proxy.supports(this, dataSource)) {
                var client = proxy.newClient(this, dataSource);
                var tm = proxy.newTransactionManager(dataSource);
                return new T2(client, tm);
            }
        }
        return new T2(this, ClientProxies.getDefaultTransactionManager());
    }

    default void tx(ThrowingConsumer<T> action) {
        tx(client -> {
            action.accept(client);
            return null;
        });
    }

    default <R> R tx(ThrowingFunction<T, R> action) {
        var proxies = ClientProxies.getProxies();
        for (var proxy : proxies) {
            var ds = proxy.getDataSource(this);
            if (ds != null) {
                return tx0(ds, action);
            }
        }
        return tx0(ClientProxies.getDefaultDataSource(), action);
    }

    default void tx(String dataSource, ThrowingConsumer<T> action) {
        tx(dataSource, client -> {
            action.accept(client);
            return null;
        });
    }

    default <R> R tx(String dataSource, ThrowingFunction<T, R> action) {
        return tx0(dataSource, action);
    }

    @SuppressWarnings("unchecked")
    private <R> R tx0(Object dataSource, ThrowingFunction<T, R> action) {
        var tup = withDataSource0(dataSource);
        var client = (T) tup.client();
        var tm = tup.transactionManager();
        var transaction = tm.getTransaction(TransactionDefinition.withDefaults());
        R result;
        try {
            result = action.apply(client);
        } catch (Throwable e) {
            log.error("An exception occurs in the transaction, rolling back", e);
            tm.rollback(transaction);
            throw new UndeclaredThrowableException(e);
        }
        tm.commit(transaction);
        return result;
    }

    record T2(Object client, PlatformTransactionManager transactionManager) {}
}
