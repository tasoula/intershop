package io.github.tasoula.payment_service.controller;

import io.github.tasoula.payment_service.exceptions.InsufficientFundsException;
import io.github.tasoula.payment_service.service.BalanceService;
import io.github.tasoula.server.domain.Amount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(BalanceController.class)
class BalanceControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BalanceService service;


    @Test
    void testBalanceUserIdGet_shouldDeniedUnauthorized() {
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100);
        when(service.getBalance(userId)).thenReturn(Mono.just(balance));

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @WithMockUser
    @Test
    void testBalanceUserIdGet_Success() {
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100);
        when(service.getBalance(userId)).thenReturn(Mono.just(balance));

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.amount").isEqualTo(100);
    }

    @WithMockUser
    @Test
    void testBalanceUserIdGet_ServerError() {
        UUID userId = UUID.randomUUID();
        when(service.getBalance(userId)).thenReturn(Mono.error(new RuntimeException("Simulated error")));

        webTestClient.get()
                .uri("/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testPaymentUserIdPost_shouldDeniedUnauthorized() {
        UUID userId = UUID.randomUUID();
        Amount amount = new Amount(BigDecimal.valueOf(20));
        when(service.withdraw(userId, amount.getAmount())).thenReturn(Mono.just(true).then()); // assume withdraw returns something if successful

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @WithMockUser
    @Test
    void testPaymentUserIdPost_Success() {
        UUID userId = UUID.randomUUID();
        Amount amount = new Amount(BigDecimal.valueOf(20));
        when(service.withdraw(userId, amount.getAmount())).thenReturn(Mono.just(true).then()); // assume withdraw returns something if successful

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .exchange()
                .expectStatus().isOk();
    }

    @WithMockUser
    @Test
    void testPaymentUserIdPost_InsufficientFunds() {
        UUID userId = UUID.randomUUID();
        Amount amount = new Amount(BigDecimal.valueOf(20));
        when(service.withdraw(userId, amount.getAmount())).thenReturn(Mono.error(new InsufficientFundsException("insufficient funds")));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYMENT_REQUIRED);
    }

    @WithMockUser
    @Test
    void testPaymentUserIdPost_UserNotFound() {
        UUID userId = UUID.randomUUID();
        Amount amount = new Amount(BigDecimal.valueOf(20));
        when(service.withdraw(userId, amount.getAmount())).thenReturn(Mono.error(new NoSuchElementException()));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .exchange()
                .expectStatus().isNotFound();
    }

    @WithMockUser
    @Test
    void testPaymentUserIdPost_ServerError() {
        UUID userId = UUID.randomUUID();
        Amount amount = new Amount(BigDecimal.valueOf(20));
        when(service.withdraw(userId, amount.getAmount())).thenReturn(Mono.error(new RuntimeException("Simulated error")));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amount)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}