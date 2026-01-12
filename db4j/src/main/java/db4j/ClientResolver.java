package db4j;

import javax.sql.DataSource;

/**
 * Pluggable client resolver (e.g. MyBatis mapper, JPA repository).
 */
public interface ClientResolver {

    boolean supports(Class<?> type);

    <T> T resolve(DataSource dataSource, Class<T> type);
}
