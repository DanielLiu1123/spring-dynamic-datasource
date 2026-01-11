package dynamicds;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public record NamedDataSource(String name, DataSource dataSource) {
    public static final boolean HIKARI_PRESENT = isPresent("com.zaxxer.hikari.HikariDataSource");

    public static NamedDataSource of(DataSource dataSource) {
        var name = datasourceName(dataSource);
        return new NamedDataSource(name, dataSource);
    }

    private static String datasourceName(DataSource dataSource) {
        if (HIKARI_PRESENT && dataSource instanceof HikariDataSource hikariDataSource) {
            return StringUtils.hasText(hikariDataSource.getPoolName())
                    ? hikariDataSource.getPoolName()
                    : String.valueOf(Objects.hashCode(dataSource));
        }
        return String.valueOf(Objects.hashCode(dataSource));
    }

    private static boolean isPresent(String className) {
        return ClassUtils.isPresent(className, NamedDataSource.class.getClassLoader());
    }
}
