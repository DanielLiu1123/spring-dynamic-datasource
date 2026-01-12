package db4j;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class DSIT {

    record User(Long id, String name) {}

    interface UserMapper {
        List<User> findAll();

        Long insert(User user);
    }

    @Container
    static final PostgreSQLContainer postgres1 = new PostgreSQLContainer("postgres:latest");

    @Container
    static final PostgreSQLContainer postgres2 = new PostgreSQLContainer("postgres:latest");

    @Test
    void readWriteSeparation() {
        var db = DB.create();
        db.register("writer", newDataSource(postgres1));
        db.register("reader", newDataSource(postgres2));

        var writerDS = db.datasource("writer");
        var readerDS = db.datasource("reader");

        writerDS.tx(tx -> {
            var mapper = tx.client(UserMapper.class);
            mapper.insert(new User(1L, "Alice"));
            mapper.insert(new User(2L, "Bob"));
            return 0;
        });

        assertThat(writerDS.client(UserMapper.class).findAll()).hasSize(2);
        assertThat(readerDS.client(UserMapper.class).findAll()).isEmpty();
    }

    static DataSource newDataSource(PostgreSQLContainer pg) {
        var ds = new HikariDataSource();
        ds.setDriverClassName(pg.getDriverClassName());
        ds.setJdbcUrl(pg.getJdbcUrl());
        ds.setUsername(pg.getUsername());
        ds.setPassword(pg.getPassword());
        return ds;
    }
}
