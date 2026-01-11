package examples.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
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
        registry.add("spring.datasource.name", () -> "postgres1");
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

    @Test
    @Transactional
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
}
