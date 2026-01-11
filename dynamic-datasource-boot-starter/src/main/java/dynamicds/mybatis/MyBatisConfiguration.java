package dynamicds.mybatis;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MybatisAutoConfiguration.class)
public class MyBatisConfiguration {
    @Bean
    static MyBatisDynamicDataSourceBeanPostProcessor myBatisDynamicDataSourceBeanPostProcessor(ApplicationContext ctx) {
        return new MyBatisDynamicDataSourceBeanPostProcessor(ctx);
    }
}
