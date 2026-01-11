package dynamicds;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

/**
 * Spring utility.
 *
 * @author Freeman
 */
final class SpringUtil {

    private SpringUtil() {}

    private static @Nullable ApplicationContext ctx;

    static void setContext(ApplicationContext applicationContext) {
        SpringUtil.ctx = applicationContext;
    }

    /**
     * Get the application context.
     *
     * @return the application context
     */
    public static ApplicationContext getContext() {
        if (ctx == null) {
            throw new IllegalStateException("You must in the Spring environment to use this method!");
        }
        return ctx;
    }
}
