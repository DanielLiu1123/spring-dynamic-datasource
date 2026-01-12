package db4j;

import java.sql.Connection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Pluggable client resolver (e.g. MyBatis mapper, JPA repository).
 */
public interface ClientResolver {

    boolean supports(Class<?> type);

    <T> T resolve(DataSource dataSource, @Nullable Connection connection, Class<T> type);
}
