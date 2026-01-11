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
    MyBatisClientProxy myBatisClientProxy(ApplicationContext ctx) {
        return new MyBatisClientProxy(ctx);
    }
}
