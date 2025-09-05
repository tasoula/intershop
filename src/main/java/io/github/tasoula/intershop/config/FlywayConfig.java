package io.github.tasoula.intershop.config;

import io.r2dbc.spi.ConnectionFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
//import org.springframework.boot.autoconfigure.r2dbc.R2dbcScriptProvider;
//import org.springframework.boot.autoconfigure.r2dbc.R2dbcScriptSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.r2dbc.connection.init.DatabasePopulator;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/*@Configuration
        @Import(R2dbcScriptProvider.class) // для определения R2dbcScriptSource
public class FlywayConfig {

    @Bean
    public Flyway flyway(ConnectionFactory connectionFactory, R2dbcProperties properties, ResourceLoader resourceLoader) {
        Flyway flyway = Flyway.configure()
                .dataSource(properties.getUrl(), properties.getUsername(), properties.getPassword()) // Используйте правильный dataSource для R2DBC
                .locations("db/migration") // Путь к миграциям
                .load();

        // Инициализация миграций R2DBC в Spring Boot
        flyway.validate(); // Проверяем миграции
        flyway.migrate(); // Запускаем миграции

        return flyway;
    }

    @Bean
    @Primary // Делаем бин основным для применения миграции
    public FlywayMigrationStrategy flywayMigrationStrategy(ObjectProvider<Flyway> flyways) {
        return strategy -> flyways.ifAvailable(flyway -> {
            flyway.validate();
            flyway.migrate();
        });
    }
}

 */
