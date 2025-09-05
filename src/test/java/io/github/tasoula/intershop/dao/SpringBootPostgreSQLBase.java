package io.github.tasoula.intershop.dao;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
public abstract class SpringBootPostgreSQLBase {

/*    @Value("${spring.database.name}")
    private String databaseName;

    @Value("${spring.database.username}")
    private String username;

    @Value("${spring.database.password}")
    private String password;

    private static PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:17.6");
        postgres.start();
    }

    @PostConstruct
    void init() {
        postgres.withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password);
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

 */
} 