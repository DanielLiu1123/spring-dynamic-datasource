package dynamicds;

import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.PlatformTransactionManager;

public interface ClientProxy {

    boolean supports(Object client, Object dataSource);

    PlatformTransactionManager newTransactionManager(Object dataSource);

    <T> T newClient(T client, Object dataSource);

    @Nullable
    DataSource getDataSource(Object client);
}
