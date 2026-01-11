package dynamicds;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.function.SingletonSupplier;

final class ClientProxies {

    private static final Logger log = LoggerFactory.getLogger(ClientProxies.class);

    private static final Supplier<List<ClientProxy>> PROXIES = SingletonSupplier.of(() -> {
        var context = SpringUtil.getContext();
        return context.getBeanProvider(ClientProxy.class).orderedStream().toList();
    });

    private static final Supplier<DataSource> DEFAULT_DATA_SOURCE = SingletonSupplier.of(() -> {
        var context = SpringUtil.getContext();
        try {
            return context.getBean("dataSource", DataSource.class);
        } catch (Exception e) {
            log.debug("Primary dataSource bean not found, trying by type.");
            return context.getBean(DataSource.class);
        }
    });

    private static final Supplier<PlatformTransactionManager> DEFAULT_TRANSACTION_MANAGER = SingletonSupplier.of(() -> {
        var context = SpringUtil.getContext();
        var tm = context.getBeanProvider(PlatformTransactionManager.class)
                .getIfUnique(() -> new DataSourceTransactionManager(Objects.requireNonNull(DEFAULT_DATA_SOURCE.get())));
        // see
        // org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration.JdbcTransactionManagerConfiguration.transactionManager
        context.getBeanProvider(TransactionManagerCustomizers.class)
                .ifAvailable(customizers -> customizers.customize(tm));
        return tm;
    });

    private ClientProxies() {}

    public static List<ClientProxy> getProxies() {
        return PROXIES.get();
    }

    public static DataSource getDefaultDataSource() {
        return DEFAULT_DATA_SOURCE.get();
    }

    public static PlatformTransactionManager getDefaultTransactionManager() {
        return DEFAULT_TRANSACTION_MANAGER.get();
    }
}
