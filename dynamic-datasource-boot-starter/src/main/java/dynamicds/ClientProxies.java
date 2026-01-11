package dynamicds;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.function.SingletonSupplier;

public final class ClientProxies {

    private static final Supplier<List<ClientProxy>> PROXIES = SingletonSupplier.of(() -> SpringUtil.getContext()
            .getBeanProvider(ClientProxy.class)
            .orderedStream()
            .toList());
    private static final Supplier<DataSource> DEFAULT_DATA_SOURCE =
            SingletonSupplier.of(() -> SpringUtil.getContext().getBean("dataSource", DataSource.class));
    private static final Supplier<PlatformTransactionManager> DEFAULT_TRANSACTION_MANAGER = SingletonSupplier.of(() -> {
        var tm = SpringUtil.getContext()
                .getBeanProvider(PlatformTransactionManager.class)
                .getIfUnique();
        if (tm == null) {
            return new DataSourceTransactionManager(Objects.requireNonNull(DEFAULT_DATA_SOURCE.get()));
        }
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
