package dynamicds;

import java.lang.reflect.Method;
import javax.sql.DataSource;

/**
 * This interface is used to switch data sources dynamically.
 *
 * @author Freeman
 * @see <a href="https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">Curiously Recurring Template Pattern</a>
 */
public interface DynamicDataSource<T extends DynamicDataSource<T>> {

    Method useDataSourceMethod = getUseDataSourceMethod();

    /**
     * Return a new/cached instance with specified {@link javax.sql.DataSource} bean name.
     *
     * @param dataSource dataSource bean name to use
     * @return new/cached instance with specified {@link javax.sql.DataSource}
     */
    @SuppressWarnings("unchecked")
    default T withDataSource(String dataSource) {
        return (T) this;
    }

    /**
     * Return a new/cached instance with specified {@link javax.sql.DataSource}.
     *
     * @param dataSource dataSource to use
     * @return new/cached instance with specified {@link javax.sql.DataSource}
     */
    @SuppressWarnings("unchecked")
    default T withDataSource(DataSource dataSource) {
        return (T) this;
    }

    private static Method getUseDataSourceMethod() {
        try {
            return DynamicDataSource.class.getMethod("withDataSource", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
