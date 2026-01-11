package dynamicds.dsprovider;

import dynamicds.NamedDataSource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.env.Environment;

/**
 * Factory for creating DataSourceProvider instances.
 */
public final class DataSourceProviders {

    private DataSourceProviders() {}

    public static List<DataSourceProvider> getProviders(Environment env) {
        var result = new ArrayList<DataSourceProvider>();
        if (NamedDataSource.HIKARI_PRESENT) {
            result.add(new HikariDataSourceProvider(env));
        }
        return result;
    }
}
