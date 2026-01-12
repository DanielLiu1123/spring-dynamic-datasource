package db4j;

import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.jspecify.annotations.Nullable;

public final class MyBatisClientResolver implements ClientResolver {

    @Override
    public boolean supports(Class<?> type) {
        return type.isInterface();
    }

    @Override
    public <T> T resolve(DataSource dataSource, @Nullable Connection connection, Class<T> type) {
        var configuration = new Configuration();
        configuration.addMapper(type);
        configuration.setEnvironment(
                new Environment(MyBatisClientResolver.class.getName(), new JdbcTransactionFactory(), dataSource));
        var sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        var ss = connection == null ? sqlSessionFactory.openSession() : sqlSessionFactory.openSession(connection);
        return sqlSessionFactory.getConfiguration().getMapper(type, ss);
    }
}
