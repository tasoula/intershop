package io.github.tasoula.payment_service.dao;

import io.github.tasoula.payment_service.model.UserBalance;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface BalanceRepository extends ReactiveCrudRepository<UserBalance, UUID> {
    Mono<UserBalance> findByUserId(UUID userId);
}
