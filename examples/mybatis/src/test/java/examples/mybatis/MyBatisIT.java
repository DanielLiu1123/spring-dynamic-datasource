package examples.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(classes = MyBatisIT.Cfg.class)
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(OutputCaptureExtension.class)
public class MyBatisIT {

    @Container
    static final PostgreSQLContainer postgres1 = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withInitScript("schema.sql");

    @Container
    static final PostgreSQLContainer postgres2 = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres1::getJdbcUrl);
        registry.add("spring.datasource.username", postgres1::getUsername);
        registry.add("spring.datasource.password", postgres1::getPassword);
        registry.add("spring.datasource.datasources[0].name", () -> "postgres2");
        registry.add("spring.datasource.datasources[0].url", postgres2::getJdbcUrl);
        registry.add("spring.datasource.datasources[0].username", postgres2::getUsername);
        registry.add("spring.datasource.datasources[0].password", postgres2::getPassword);
    }

    @Autowired
    UserMapper userMapper;

    @AfterEach
    void cleanup() {
        userMapper.deleteAllUsers();
        userMapper.withDataSource("postgres2").deleteAllUsers();
    }

    @Test
    void insertToPostgres1_thenReadFromPostgres2_shouldReturnEmptyList() {
        // Insert a record to postgres1
        userMapper.insertUser(new User(1L, "Alice"));
        assertThat(userMapper.findAllUsers()).containsExactlyInAnyOrder(new User(1L, "Alice"));

        // Read from postgres2, should be empty
        assertThat(userMapper.withDataSource("postgres2").findAllUsers()).isEmpty();

        // Insert a record to postgres2
        userMapper.withDataSource("postgres2").insertUser(new User(2L, "Bob"));
        assertThat(userMapper.withDataSource("postgres2").findAllUsers())
                .containsExactlyInAnyOrder(new User(2L, "Bob"));
    }

    @Test
    void whenExceptionThrownInTransaction_thenRollback() {
        assertThatCode(() -> userMapper.withTransaction(mapper -> {
                    mapper.insertUser(new User(1L, "Alice"));
                    mapper.insertUser(new User(2L, "Bob"));
                    throw new RuntimeException("Test exception");
                }))
                .isInstanceOf(RuntimeException.class);
        assertThat(userMapper.findAllUsers()).isEmpty();

        assertThatCode(() -> userMapper.withDataSource("postgres2").withTransaction(mapper -> {
                    mapper.insertUser(new User(3L, "Charlie"));
                    mapper.insertUser(new User(4L, "David"));
                    throw new RuntimeException("Test exception");
                }))
                .isInstanceOf(RuntimeException.class);
        assertThat(userMapper.withDataSource("postgres2").findAllUsers()).isEmpty();
    }

    @Test
    void whenUseDataSourceBeanNameToLookup_shouldNotFindAnyDataSource(CapturedOutput output) {
        userMapper.insertUser(new User(1L, "Alice"));

        assertThat(userMapper.withDataSource("dataSource").findAllUsers())
                .containsExactlyInAnyOrder(new User(1L, "Alice"));
        assertThat(output.getOut()).contains("dataSource 'dataSource' not found, available dataSources:");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @MapperScan("examples.mybatis")
    static class Cfg {}
}
