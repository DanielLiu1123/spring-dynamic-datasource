package dynamicds.dsprovider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dynamicds.DataSourcesProperties;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Default implementation of {@link DataSourceProvider} that creates {@link HikariDataSource}.
 */
class HikariDataSourceProvider implements DataSourceProvider {

    private final HikariConfig defaultHikariConfig;

    public HikariDataSourceProvider(Environment environment) {
        this.defaultHikariConfig = Binder.get(environment).bindOrCreate("spring.datasource.hikari", HikariConfig.class);
    }

    @Override
    public Class<? extends DataSource> getType() {
        return HikariDataSource.class;
    }

    @Override
    public DataSource createDataSource(DataSourcesProperties.DataSource properties) {
        var hc = properties.newHikariConfig(defaultHikariConfig);
        return new HikariDataSource(hc);
    }
}
