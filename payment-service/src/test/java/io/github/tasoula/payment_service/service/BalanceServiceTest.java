package io.github.tasoula.payment_service.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.tasoula.payment_service.dao.BalanceRepository;
import io.github.tasoula.payment_service.exceptions.InsufficientFundsException;
import io.github.tasoula.payment_service.model.UserBalance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private BalanceRepository repository;

    @InjectMocks
    private BalanceService balanceService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getBalance_UserExists_ReturnsBalance() {
        // Arrange
        BigDecimal initialBalance = BigDecimal.valueOf(5000);
        UserBalance userBalance = new UserBalance(UUID.randomUUID(), userId, initialBalance);
        when(repository.findByUserId(userId)).thenReturn(Mono.just(userBalance));
        when(repository.save(any(UserBalance.class))).thenReturn(Mono.just(userBalance));

        // Act & Assert
        StepVerifier.create(balanceService.getBalance(userId))
                .expectNext(initialBalance)
                .verifyComplete();
    }

    @Test
    void getBalance_UserDoesNotExist_CreatesAndReturnsInitialBalance() {
        // Arrange
        BigDecimal initialBalance = BigDecimal.valueOf(10000);
        UserBalance newUserBalance = new UserBalance(null, userId, initialBalance);
        when(repository.findByUserId(userId)).thenReturn(Mono.empty());
        when(repository.save(any(UserBalance.class))).thenReturn(Mono.just(newUserBalance));

        // Act & Assert
        StepVerifier.create(balanceService.getBalance(userId))
                .expectNext(initialBalance)
                .verifyComplete();

        verify(repository, times(1)).save(argThat(ub -> ub.getUserId().equals(userId) && ub.getBalance().equals(initialBalance)));
    }

    @Test
    void withdraw_SufficientFunds_WithdrawsAmount() {
        // Arrange
        BigDecimal initialBalance = BigDecimal.valueOf(5000);
        BigDecimal withdrawAmount = BigDecimal.valueOf(1000);
        UserBalance userBalance = new UserBalance(UUID.randomUUID(), userId, initialBalance);
        UserBalance updatedUserBalance = new UserBalance(userBalance.getId(), userId, initialBalance.subtract(withdrawAmount));

        when(repository.findByUserId(userId)).thenReturn(Mono.just(userBalance));
        when(repository.save(any(UserBalance.class))).thenReturn(Mono.just(updatedUserBalance));

        // Act & Assert
        StepVerifier.create(balanceService.withdraw(userId, withdrawAmount))
                .verifyComplete();

        verify(repository, times(1)).save(argThat(ub -> ub.getBalance().equals(initialBalance.subtract(withdrawAmount))));
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        // Arrange
        BigDecimal initialBalance = BigDecimal.valueOf(500);
        BigDecimal withdrawAmount = BigDecimal.valueOf(1000);
        UserBalance userBalance = new UserBalance(UUID.randomUUID(), userId, initialBalance);
        when(repository.findByUserId(userId)).thenReturn(Mono.just(userBalance));

        // Act & Assert
        StepVerifier.create(balanceService.withdraw(userId, withdrawAmount))
                .expectError(InsufficientFundsException.class)
                .verify();

        verify(repository, never()).save(any(UserBalance.class));
    }

    @Test
    void withdraw_UserNotFound_ThrowsException() {
        // Arrange
        BigDecimal withdrawAmount = BigDecimal.valueOf(1000);
        when(repository.findByUserId(userId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(balanceService.withdraw(userId, withdrawAmount))
                .expectError(NoSuchElementException.class)
                .verify();

        verify(repository, never()).save(any(UserBalance.class));
    }
}