package dynamicds;

import java.lang.reflect.UndeclaredThrowableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;

final class TransactionSupport {

    private static final Logger log = LoggerFactory.getLogger(TransactionSupport.class);

    private TransactionSupport() {}

    static ClientContext resolveContext(Object self, Object dataSource) {
        var proxies = Suppliers.getProxies();
        for (var proxy : proxies) {
            if (proxy.supports(self, dataSource)) {
                var client = proxy.createClient(self, dataSource);
                var tm = proxy.createTransactionManager(dataSource);
                return new ClientContext(client, tm);
            }
        }
        return new ClientContext(self, Suppliers.getDefaultTransactionManager());
    }

    static Object resolveClient(Object self, Object dataSource) {
        var proxies = Suppliers.getProxies();
        for (var proxy : proxies) {
            if (proxy.supports(self, dataSource)) {
                return proxy.createClient(self, dataSource);
            }
        }
        log.warn(
                "dataSource '{}' not found, available dataSources: {}",
                dataSource,
                Suppliers.getDataSources().keySet());
        return self;
    }

    @SuppressWarnings("unchecked")
    static <T extends DynamicDataSource<T>, R> R executeInTransaction(
            T self, Object dataSource, TransactionDefinition definition, ThrowingFunction<T, R> action) {
        var context = resolveContext(self, dataSource);
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

    static RuntimeException wrapRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new UndeclaredThrowableException(ex);
    }
}
