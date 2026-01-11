package dynamicds;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Register multiple DataSource bean definitions based on DataSourcesProperties.
 *
 * @author Freeman
 */
class DataSourcesBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor {

    private final Environment env;

    DataSourcesBeanDefinitionRegistry(Environment env) {
        this.env = env;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        var properties = Binder.get(env).bindOrCreate(DataSourcesProperties.PREFIX, DataSourcesProperties.class);
        if (properties.datasources().isEmpty()) {
            return;
        }

        var hikariConfig = Binder.get(env).bindOrCreate("spring.datasource.hikari", HikariConfig.class);

        for (var ds : properties.datasources()) {
            var bd = BeanDefinitionBuilder.rootBeanDefinition(HikariDataSource.class, () -> {
                        // Use com.zaxxer.hikari.HikariDataSource.HikariDataSource(com.zaxxer.hikari.HikariConfig)
                        // to explicitly trigger the initialization logic
                        // so that potential errors can be detected earlier (shift left).
                        var hc = ds.newHikariConfig(hikariConfig);
                        return new HikariDataSource(hc);
                    })
                    .getBeanDefinition();
            registry.registerBeanDefinition(ds.name(), bd);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
