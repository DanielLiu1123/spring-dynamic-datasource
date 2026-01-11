package dynamicds;

import java.util.Objects;
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
    default T use(String dataSource) {
        return (T) TransactionSupport.resolveClient(this, dataSource);
    }

    default void txDo(ThrowingConsumer<T> action) {
        txGet(t -> {
            action.accept(t);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    default <R> R txGet(ThrowingFunction<T, R> action) {
        var client = (T) this;
        var dataSource = Suppliers.getProxies().stream()
                .map(p -> p.getDataSource(client))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(Suppliers::getDefaultDataSource);
        var definition = TransactionDefinition.withDefaults();
        return TransactionSupport.executeInTransaction(client, dataSource, definition, action);
    }
}
