package io.github.tasoula.payment_service.service;

import io.github.tasoula.payment_service.dao.BalanceRepository;
import io.github.tasoula.payment_service.exceptions.InsufficientFundsException;
import io.github.tasoula.payment_service.model.UserBalance;
import io.github.tasoula.server.domain.Amount;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BalanceService {
    private final BalanceRepository repository;

    public BalanceService(BalanceRepository repository) {
        this.repository = repository;
    }

    public Mono<BigDecimal> getBalance(UUID userId) {
        return repository.findByUserId(userId)
                .map(UserBalance::getBalance)
                .switchIfEmpty(
                        repository.save(new UserBalance(null, userId, BigDecimal.valueOf(10000)))
                                .map(UserBalance::getBalance)
                );
    }

    public Mono<Void> withdraw(UUID userId, BigDecimal amount) {
        return repository.findByUserId(userId)
                .switchIfEmpty(
                        Mono.error(new NoSuchElementException("Balance not found for user ID: " + userId))
                )
                .flatMap(userBalance -> {
                    BigDecimal balance = userBalance.getBalance();
                    if (balance.compareTo(amount) < 0) {
                        return Mono.error(new InsufficientFundsException("Insufficient funds for user ID: " + userId));
                    }
                    userBalance.setBalance(balance.subtract(amount));
                    return repository.save(userBalance)
                            .then();
                });
    }
}

