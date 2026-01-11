package dynamicds;

import com.zaxxer.hikari.HikariDataSource;
import dynamicds.dsprovider.DataSourceProvider;
import dynamicds.dsprovider.DataSourceProviders;
import java.util.List;
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
    private final List<DataSourceProvider> dataSourceProviders;

    DataSourcesBeanDefinitionRegistry(Environment env) {
        this.env = env;
        this.dataSourceProviders = DataSourceProviders.getProviders(env);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        var binder = Binder.get(env);
        var properties = binder.bindOrCreate(DataSourcesProperties.PREFIX, DataSourcesProperties.class);
        var mainDataSourceType = getMainDataSourceType(binder);
        if (properties.datasources().isEmpty()) {
            return;
        }

        for (var ds : properties.datasources()) {
            var type = ds.type() != null ? ds.type() : mainDataSourceType;
            var provider = dataSourceProviders.stream()
                    .filter(p -> p.supports(type))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalStateException("No DataSourceProvider found for type: " + type.getName()));

            var bd = BeanDefinitionBuilder.rootBeanDefinition(DataSource.class, () -> provider.createDataSource(ds))
                    .getBeanDefinition();

            var beanName = "dataSource#" + ds.name();
            registry.registerBeanDefinition(beanName, bd);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends DataSource> getMainDataSourceType(Binder binder) {
        return binder.bind("spring.datasource.type", Class.class).orElse(HikariDataSource.class);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}
}
