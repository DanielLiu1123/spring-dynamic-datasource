package dynamicds.dsprovider;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

/**
 * Factory for creating DataSourceProvider instances.
 */
public final class DataSourceProviders {

    private DataSourceProviders() {}

    public static List<DataSourceProvider> getProviders(Environment env) {
        var result = new ArrayList<DataSourceProvider>();
        if (isPresent("com.zaxxer.hikari.HikariDataSource")) {
            result.add(new HikariDataSourceProvider(env));
        }
        return result;
    }

    private static boolean isPresent(String className) {
        return ClassUtils.isPresent(className, DataSourceProviders.class.getClassLoader());
    }
}
