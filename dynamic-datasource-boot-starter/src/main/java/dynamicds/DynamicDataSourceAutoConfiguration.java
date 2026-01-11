package dynamicds;

import dynamicds.mybatis.MyBatisConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnDataSources
@EnableConfigurationProperties(DataSourcesProperties.class)
@Import(MyBatisConfiguration.class)
public class DynamicDataSourceAutoConfiguration {

    public DynamicDataSourceAutoConfiguration(ApplicationContext ctx) {
        SpringUtil.setContext(ctx);
    }

    @Bean
    static DataSourcesBeanDefinitionRegistry dataSourcesBeanDefinitionRegistry(Environment environment) {
        return new DataSourcesBeanDefinitionRegistry(environment);
    }
}
