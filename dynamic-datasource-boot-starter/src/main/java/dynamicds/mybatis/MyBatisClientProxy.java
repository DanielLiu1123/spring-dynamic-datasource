package dynamicds.mybatis;

import dynamicds.ClientProxy;
import dynamicds.NamedDataSource;
import dynamicds.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jspecify.annotations.Nullable;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

class MyBatisClientProxy implements ClientProxy, SmartInitializingSingleton {
    private static final Logger log = LoggerFactory.getLogger(MyBatisClientProxy.class);

    private final ConcurrentMap<String, Object> mappers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SqlSessionTemplate> sqlSessionTemplates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PlatformTransactionManager> transactionManagers = new ConcurrentHashMap<>();
    private final ConcurrentMap<DataSource, Set<Object>> dataSourceToClients = new ConcurrentHashMap<>();

    private final ConfigurableApplicationContext ctx;
    private final Map<String, NamedDataSource> dataSources = new HashMap<>();

    public MyBatisClientProxy(ApplicationContext ctx) {
        this.ctx = (ConfigurableApplicationContext) ctx;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.dataSources.putAll(Suppliers.getDataSources());
    }

    @Override
    public boolean supports(Object client, Object dataSource) {
        var isProxied = AopProxyUtils.proxiedUserInterfaces(client).length > 0;
        if (!isProxied) {
            return false;
        }
        return hasDataSource(dataSource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createClient(T client, Object dataSource) {
        return (T) getOrRegisterMapper(client, determineDataSource(dataSource));
    }

    @Override
    public @Nullable DataSource getDataSource(Object client) {
        for (var entry : dataSourceToClients.entrySet()) {
            if (entry.getValue().contains(client)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public PlatformTransactionManager createTransactionManager(Object dataSource) {
        return getOrRegisterTransactionManager(determineDataSource(dataSource));
    }

    private boolean hasDataSource(Object dataSource) {
        if (dataSource instanceof String name) {
            return dataSources.containsKey(name);
        } else if (dataSource instanceof DataSource) {
            return true;
        } else {
            throw new IllegalArgumentException("Unsupported dataSource type: " + dataSource.getClass());
        }
    }

    private NamedDataSource determineDataSource(Object dataSource) {
        if (dataSource instanceof String name) {
            return Objects.requireNonNull(dataSources.get(name));
        } else if (dataSource instanceof DataSource ds) {
            return NamedDataSource.of(ds);
        } else {
            throw new IllegalArgumentException("Unsupported dataSource type: " + dataSource.getClass());
        }
    }

    private PlatformTransactionManager getOrRegisterTransactionManager(NamedDataSource namedDataSource) {
        var datasourceName = namedDataSource.name();
        var datasource = namedDataSource.dataSource();

        var beanName = "txManager#" + datasourceName;

        return transactionManagers.computeIfAbsent(beanName, k -> {
            var tm = new DataSourceTransactionManager(datasource);
            registerSingleton(beanName, tm);
            return tm;
        });
    }

    private Object getOrRegisterMapper(Object client, NamedDataSource namedDataSource) {
        var datasourceName = namedDataSource.name();
        var datasource = namedDataSource.dataSource();

        var mapperInterface = AopProxyUtils.proxiedUserInterfaces(client)[0];
        var mapperBeanName = mapperInterface.getName() + "#" + datasourceName;

        var result = mappers.computeIfAbsent(mapperBeanName, k -> {
            var sqlSessionTemplate = getOrRegisterSqlSessionTemplate(datasourceName, datasource, mapperInterface);
            var mapper = sqlSessionTemplate.getMapper(mapperInterface);
            registerSingleton(mapperBeanName, mapper);
            if (log.isDebugEnabled()) {
                log.debug("Registered mapper {}", mapperBeanName);
            }
            return mapper;
        });

        dataSourceToClients
                .computeIfAbsent(datasource, k -> ConcurrentHashMap.newKeySet())
                .add(result);

        if (log.isDebugEnabled()) {
            log.debug("Found existing mapper {}", mapperBeanName);
        }

        return result;
    }

    private SqlSessionTemplate getOrRegisterSqlSessionTemplate(
            String datasourceName, DataSource dataSource, Class<?> mapperInterface) {
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
}
