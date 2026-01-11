package dynamicds;

import org.springframework.transaction.TransactionDefinition;

/**
 * This interface is used to switch data sources dynamically.
 *
 * @author Freeman
 * @see <a href="https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">Curiously Recurring Template Pattern</a>
 */
public interface DynamicDataSource<T extends DynamicDataSource<T>> {

    /**
     * Return a new/cached instance with specified {@link javax.sql.DataSource} bean name.
     *
     * @param dataSource dataSource bean name to use
     * @return new/cached instance with specified {@link javax.sql.DataSource}
     */
    @SuppressWarnings("unchecked")
    default T withDataSource(String dataSource) {
        return (T) TransactionSupport.resolveContext(this, dataSource).client();
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

    @SuppressWarnings("unchecked")
    default <R> R withTransactionResult(TransactionDefinition definition, ThrowingFunction<T, R> action) {
        var proxies = ClientProxies.getProxies();
        for (var proxy : proxies) {
            var ds = proxy.getDataSource(this);
            if (ds != null) {
                return TransactionSupport.executeInTransaction((T) this, ds, definition, action);
            }
        }
        return TransactionSupport.executeInTransaction(
                (T) this, ClientProxies.getDefaultDataSource(), definition, action);
    }
}
