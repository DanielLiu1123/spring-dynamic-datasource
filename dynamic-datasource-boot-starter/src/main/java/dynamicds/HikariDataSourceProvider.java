package dynamicds;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Default implementation of {@link DataSourceProvider} that creates {@link HikariDataSource}.
 */
public class HikariDataSourceProvider implements DataSourceProvider {

    private final HikariConfig defaultHikariConfig;

    public HikariDataSourceProvider(Environment environment) {
        this.defaultHikariConfig = Binder.get(environment).bindOrCreate("spring.datasource.hikari", HikariConfig.class);
    }

    @Override
    public DataSource createDataSource(DataSourcesProperties.DataSource properties) {
        var hc = properties.newHikariConfig(defaultHikariConfig);
        return new HikariDataSource(hc);
    }
}
