package io.github.tasoula.payment_service.controller;

import io.github.tasoula.payment_service.exceptions.InsufficientFundsException;
import io.github.tasoula.payment_service.service.BalanceService;
import io.github.tasoula.server.api.DefaultApi;
import io.github.tasoula.server.domain.Amount;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class BalanceController implements DefaultApi {

    private final BalanceService service;

    public BalanceController(BalanceService service) {
        this.service = service;
    }

    @Override
    public Mono<ResponseEntity<Amount>> balanceUserIdGet(
            @Parameter(name = "userId", description = "UUID пользователя", required = true, in = ParameterIn.PATH) @PathVariable("userId") UUID userId,
            @Parameter(hidden = true) final ServerWebExchange exchange
    )  {
        return service.getBalance(userId)
                .map(Amount::new)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Amount>build()); // 500
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> paymentUserIdPost(
            @Parameter(name = "userId", description = "UUID пользователя", required = true, in = ParameterIn.PATH) @PathVariable("userId") UUID userId,
            @Parameter(name = "Amount", description = "", required = true) @Valid @RequestBody Mono<Amount> amount,
            @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return amount.flatMap(sum -> service.withdraw(userId, sum.getAmount()))
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> {
                    if (throwable instanceof InsufficientFundsException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).<Void>build()); // 402
                    } else if (throwable instanceof NoSuchElementException) {
                        return Mono.just(ResponseEntity.notFound().build()); // 404
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build()); // 500
                    }
                });
    }
}


