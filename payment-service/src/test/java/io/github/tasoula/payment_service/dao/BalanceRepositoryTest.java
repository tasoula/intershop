package io.github.tasoula.payment_service.dao;

import io.github.tasoula.payment_service.model.UserBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
class BalanceRepositoryTest {
    @Container // декларируем объект учитываемым тест-контейнером
    @ServiceConnection // автоматически назначаем параметры соединения с контейнером
    static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:17.6");

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private DatabaseClient databaseClient; // Для очистки и инициализации базы

    private UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        balanceRepository.deleteAll().block();
    }

    @Test
    void findByUserId_whenUserBalanceExists_shouldReturnUserBalance() {
        // Arrange
        UserBalance expectedBalance = new UserBalance(null, userId, BigDecimal.valueOf(100.00));
        balanceRepository.save(expectedBalance).block(); // block() здесь, потому что это integration test и нам нужно дождаться завершения операции

        // Act
        Mono<UserBalance> actualBalanceMono = balanceRepository.findByUserId(userId);

        // Assert
        StepVerifier.create(actualBalanceMono)
                .assertNext(actualBalance -> {
                    assertThat(actualBalance.getUserId()).isEqualTo(userId);
                    assertThat(actualBalance.getBalance()).isEqualByComparingTo(expectedBalance.getBalance());
                })
                .verifyComplete();
    }


    @Test
    void findByUserId_whenUserBalanceDoesNotExist_shouldReturnEmptyMono() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();

        // Act
        Mono<UserBalance> actualBalanceMono = balanceRepository.findByUserId(nonExistentUserId);

        // Assert
        StepVerifier.create(actualBalanceMono)
                .verifyComplete(); // Проверяем, что Mono пустой
    }

    @Test
    void save_shouldSaveBalanceCorrectly() {
        // Arrange
        UserBalance userBalanceToSave = new UserBalance(null, userId, BigDecimal.valueOf(50.00));

        // Act
        Mono<UserBalance> savedBalanceMono = balanceRepository.save(userBalanceToSave);

        // Assert
        StepVerifier.create(savedBalanceMono)
                .assertNext(savedBalance -> {
                    assertThat(savedBalance.getUserId()).isEqualTo(userId);
                    assertThat(savedBalance.getBalance()).isEqualTo(userBalanceToSave.getBalance());
                    assertThat(savedBalance.getId()).isNotNull(); // Проверяем, что ID был сгенерирован
                })
                .verifyComplete();


        // Дополнительная проверка: убедимся, что данные действительно сохранены в базе
        UserBalance balanceFromDb = balanceRepository.findByUserId(userId).block();
        assertThat(balanceFromDb).isNotNull();
        assertThat(balanceFromDb.getBalance()).isEqualByComparingTo(userBalanceToSave.getBalance());
    }


    @Test
    void save_and_findById_shouldWork() {
        // Arrange
        UserBalance userBalanceToSave = new UserBalance(null, userId, BigDecimal.valueOf(75.00));

        // Act
        UserBalance savedBalance = balanceRepository.save(userBalanceToSave).block();

        // Assert
        assertThat(savedBalance).isNotNull();
        UUID savedBalanceId = savedBalance.getId();

        Mono<UserBalance> foundBalanceMono = balanceRepository.findById(savedBalanceId);

        StepVerifier.create(foundBalanceMono)
                .assertNext(foundBalance -> {
                    assertThat(foundBalance.getId()).isEqualTo(savedBalanceId);
                    assertThat(foundBalance.getUserId()).isEqualTo(userId);
                    assertThat(foundBalance.getBalance()).isEqualByComparingTo(userBalanceToSave.getBalance());
                })
                .verifyComplete();
    }
}