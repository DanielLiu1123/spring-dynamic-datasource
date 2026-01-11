package dynamicds;

import com.zaxxer.hikari.HikariConfig;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;

@ConfigurationProperties(DataSourcesProperties.PREFIX)
public record DataSourcesProperties(List<DataSource> datasources) {

    public static final String PREFIX = "spring.datasource";

    public DataSourcesProperties {
        datasources = datasources != null ? datasources : List.of();
    }

    public record DataSource(
            String name, String driverClassName, String url, String username, String password, Hikari hikari) {

        public DataSource {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(url, "url must not be null");
            Objects.requireNonNull(username, "username must not be null");
            Objects.requireNonNull(password, "password must not be null");
            driverClassName = driverClassName != null
                    ? driverClassName
                    : DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
            Objects.requireNonNull(driverClassName, "driverClassName must not be null");
            hikari = hikari != null ? hikari : new Hikari(null, null);
        }

        public HikariConfig newHikariConfig(HikariConfig hikariConfig) {
            var maximumPoolSize = this.hikari.maximumPoolSize() != null
                    ? this.hikari.maximumPoolSize()
                    : hikariConfig.getMaximumPoolSize();
            var minimumIdle =
                    this.hikari.minimumIdle() != null ? this.hikari.minimumIdle() : hikariConfig.getMinimumIdle();
            var result = new HikariConfig();
            result.setPoolName(name);
            result.setDriverClassName(driverClassName);
            result.setJdbcUrl(url);
            result.setUsername(username);
            result.setPassword(password);
            if (maximumPoolSize > 0) {
                result.setMaximumPoolSize(maximumPoolSize);
            }
            if (minimumIdle > 0) {
                result.setMinimumIdle(minimumIdle);
            }
            return result;
        }

        public record Hikari(
                @Nullable Integer maximumPoolSize, @Nullable Integer minimumIdle) {}
    }
}
