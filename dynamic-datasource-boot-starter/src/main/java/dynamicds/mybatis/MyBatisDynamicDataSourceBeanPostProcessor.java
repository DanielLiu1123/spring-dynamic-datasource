package dynamicds.mybatis;

import dynamicds.DynamicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

/**
 * Proxy {@link DynamicDataSource#withDataSource(String)} to create a new instance of mapper with a specified data source.
 *
 * @author Freeman
 */
public class MyBatisDynamicDataSourceBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext ctx;

    public MyBatisDynamicDataSourceBeanPostProcessor(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicDataSource<?> mapper) {
            return MyBatisDynamicDataSourceMethodInterceptor.createProxy(mapper, ctx);
        }
        return bean;
    }
}
