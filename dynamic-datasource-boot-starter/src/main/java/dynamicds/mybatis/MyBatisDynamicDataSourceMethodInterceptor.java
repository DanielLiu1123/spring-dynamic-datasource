package dynamicds.mybatis;

import dynamicds.DynamicDataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

/**
 * @author Freeman
 */
final class MyBatisDynamicDataSourceMethodInterceptor implements MethodInterceptor {
    private static final Logger log = LoggerFactory.getLogger(MyBatisDynamicDataSourceMethodInterceptor.class);

    private static final ConcurrentMap<String, SqlSessionTemplate> sqlSessionTemplates = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Object> mappers = new ConcurrentHashMap<>();

    private final Object originMapper;
    private final ConfigurableApplicationContext ctx;
    private final Class<?> mapperInterface;

    MyBatisDynamicDataSourceMethodInterceptor(Object originMapper, ApplicationContext ctx) {
        this.originMapper = originMapper;
        this.mapperInterface = AopProxyUtils.proxiedUserInterfaces(originMapper)[0];
        this.ctx = (ConfigurableApplicationContext) ctx;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (Objects.equals(method, DynamicDataSource.useDataSourceMethod)
                && invocation instanceof ProxyMethodInvocation pmi) {
            return getOrRegisterMapper(pmi);
        }

        ReflectionUtils.makeAccessible(method);

        try {
            return method.invoke(originMapper, invocation.getArguments());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object getOrRegisterMapper(ProxyMethodInvocation invocation) {
        var datasourceName = Objects.requireNonNull((String) invocation.getArguments()[0]);

        DataSource datasource;
        try {
            datasource = ctx.getBean(datasourceName, DataSource.class);
        } catch (BeansException e) {
            log.error(
                    "No such datasource: {}, available datasource(s): {}",
                    datasourceName,
                    ctx.getBeanNamesForType(DataSource.class));
            return invocation.getProxy();
        }

        var mapperBeanName = mapperInterface.getName() + "#" + datasourceName;

        var result = mappers.computeIfAbsent(mapperBeanName, k -> {
            var sqlSessionTemplate = getOrRegisterSqlSessionTemplate(datasourceName, datasource);
            var mapper = registerMapper(sqlSessionTemplate, mapperBeanName);
            if (log.isDebugEnabled()) {
                log.debug("Registered mapper {}", mapperBeanName);
            }
            return mapper;
        });

        if (log.isDebugEnabled()) {
            log.debug("Found existing mapper {}", mapperBeanName);
        }

        return result;
    }

    private Object registerMapper(SqlSessionTemplate sqlSessionTemplate, String beanName) {
        Object mapper = sqlSessionTemplate.getMapper(mapperInterface);
        var proxy = createProxy(mapper, ctx);

        registerSingleton(beanName, proxy);

        return proxy;
    }

    private SqlSessionTemplate getOrRegisterSqlSessionTemplate(String datasourceName, DataSource dataSource) {
        var sstBeanName = "sqlSessionTemplate#" + datasourceName;

        var result = sqlSessionTemplates.computeIfAbsent(sstBeanName, k -> {
            var sst = registerSqlSessionTemplate(dataSource, sstBeanName);
            if (log.isDebugEnabled()) {
                log.debug("Registered SqlSessionTemplate {}", sstBeanName);
            }
            return sst;
        });

        if (log.isDebugEnabled()) {
            log.debug("Found existing SqlSessionTemplate {}", sstBeanName);
        }

        var configuration = result.getConfiguration();
        if (!configuration.hasMapper(mapperInterface)) {
            synchronized (configuration) {
                if (!configuration.hasMapper(mapperInterface)) {
                    configuration.addMapper(mapperInterface);
                }
            }
        }

        return result;
    }

    private SqlSessionTemplate registerSqlSessionTemplate(DataSource dataSource, String sstBeanName) {
        var mybatisAutoConfiguration = ctx.getAutowireCapableBeanFactory().createBean(MybatisAutoConfiguration.class);
        SqlSessionFactory sqlSessionFactory;
        try {
            sqlSessionFactory = mybatisAutoConfiguration.sqlSessionFactory(dataSource);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SqlSessionFactory", e);
        }
        var sqlSessionTemplate = mybatisAutoConfiguration.sqlSessionTemplate(sqlSessionFactory);

        registerSingleton(sstBeanName, sqlSessionTemplate);

        return sqlSessionTemplate;
    }

    private void registerSingleton(String beanName, Object bean) {
        ctx.getBeanFactory().registerSingleton(beanName, bean);
    }

    static Object createProxy(Object originMapper, ApplicationContext ctx) {
        var interfaces = AopProxyUtils.proxiedUserInterfaces(originMapper);
        var proxyFactory = new ProxyFactory(interfaces);
        proxyFactory.addAdvice(new MyBatisDynamicDataSourceMethodInterceptor(originMapper, ctx));
        return proxyFactory.getProxy();
    }
}
