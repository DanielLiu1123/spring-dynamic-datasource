package dynamicds;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.function.SingletonSupplier;

public final class Suppliers {

    private static final Logger log = LoggerFactory.getLogger(Suppliers.class);

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

    private static final Supplier<Map<String, NamedDataSource>> DATA_SOURCES = SingletonSupplier.of(() -> {
        var context = SpringUtil.getContext();
        return context.getBeanProvider(DataSource.class).stream()
                .map(NamedDataSource::of)
                .collect(Collectors.toMap(NamedDataSource::name, Function.identity()));
    });

    private Suppliers() {}

    public static List<ClientProxy> getProxies() {
        return PROXIES.get();
    }

    public static DataSource getDefaultDataSource() {
        return DEFAULT_DATA_SOURCE.get();
    }

    public static PlatformTransactionManager getDefaultTransactionManager() {
        return DEFAULT_TRANSACTION_MANAGER.get();
    }

    public static Map<String, NamedDataSource> getDataSources() {
        return DATA_SOURCES.get();
    }
}
