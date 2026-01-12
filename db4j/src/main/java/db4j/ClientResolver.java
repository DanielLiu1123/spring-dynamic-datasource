package db4j;

/**
 * Pluggable client resolver (e.g. MyBatis mapper, JPA repository).
 */
public interface ClientResolver {

    boolean supports(Class<?> type);

    <T> T resolve(ResolveContext ctx, Class<T> type);
}
