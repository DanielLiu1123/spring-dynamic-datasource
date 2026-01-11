package dynamicds;

import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Register multiple DataSource bean definitions based on DataSourcesProperties.
 */
class DataSourcesBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor {

    private final Environment env;
    private final DataSourceProvider dataSourceProvider;

    DataSourcesBeanDefinitionRegistry(Environment env) {
        this.env = env;
        this.dataSourceProvider = new HikariDataSourceProvider(env);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        var properties = Binder.get(env).bindOrCreate(DataSourcesProperties.PREFIX, DataSourcesProperties.class);
        if (properties.datasources().isEmpty()) {
            return;
        }

        for (var ds : properties.datasources()) {
            var bd = BeanDefinitionBuilder.rootBeanDefinition(
                            DataSource.class, () -> dataSourceProvider.createDataSource(ds))
                    .getBeanDefinition();
            registry.registerBeanDefinition(ds.name(), bd);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
