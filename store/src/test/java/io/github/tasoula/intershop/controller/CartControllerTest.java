package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.github.tasoula.intershop.interceptor.CookieConst.USER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@WebFluxTest(value = CartController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserInterceptor.class))
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;


    @Test
    void viewCart_shouldReturnCartViewWithItemsAndTotal() throws Exception {
        UUID userId = UUID.randomUUID();
        List<ProductDto> items = List.of(new ProductDto(
                                                UUID.randomUUID(),
                                                "Product 1",
                                                "Description 1",
                                                BigDecimal.valueOf(100.0),
                                                5,
                                                2));
        BigDecimal total = BigDecimal.valueOf(200.0);

        when(cartService.findByUserId(userId)).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(total));
        when(cartService.isAvailable(userId)).thenReturn(Mono.just(true));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("userId", userId.toString())
                        .build())
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());
                    assertTrue(body.contains("<title>Корзина товаров</title>"), "page title not found in template");
                    assertTrue(body.contains(items.getFirst().getTitle()), "product1 not found in template");
                    assertTrue(body.contains("<b>100.0 руб.</b>"), "product1 price not found in template");
                    assertTrue(body.contains("<b id=\"total-price\">Итого: 200.0 руб.</b>"), "total summ not found in template");

                });
    }


    @Test
    void viewCart_shouldReturnCartViewWithEmptyCart() throws Exception {
        UUID userId = UUID.randomUUID();
        List<ProductDto> items = List.of();
        BigDecimal total = BigDecimal.ZERO;

        when(cartService.findByUserId(userId)).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(total));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("userId", userId.toString())
                        .build())
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());
                    assertTrue(body.contains("<title>Корзина товаров</title>"), "page title not found in template");
                    assertTrue(body.contains("<b id=\"total-price\">Итого: 0 руб.</b>"), "total summ not found in template");

                });
    }

    @Test
    void getTotal_returnsTotalPrice() {
        UUID userId = UUID.randomUUID();
        BigDecimal total = BigDecimal.valueOf(42.50);

        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(total));

        webTestClient.get()
                .uri("/cart/total")
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(total);
    }

    @Test
    void isEmpty_returnsTrueWhenCartIsEmpty() {
        UUID userId = UUID.randomUUID();

        when(cartService.isEmpty(userId)).thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/cart/is_empty")
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void isEmpty_returnsFalseWhenCartIsNotEmpty() {
        UUID userId = UUID.randomUUID();

        when(cartService.isEmpty(userId)).thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/cart/is_empty")
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void changeProductQuantityInCart_plusAction_returnsUpdatedQuantity() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int updatedQuantity = 5;

        when(cartService.changeProductQuantityInCart(userId, productId, 1)).thenReturn(Mono.just(updatedQuantity));

        webTestClient.post()
                .uri("/cart/items/{id}?action=PLUS", productId)
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(updatedQuantity);
    }

    @Test
    void changeProductQuantityInCart_minusAction_returnsUpdatedQuantity() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int updatedQuantity = 3;

        when(cartService.changeProductQuantityInCart(userId, productId, -1)).thenReturn(Mono.just(updatedQuantity));

        webTestClient.post()
                .uri("/cart/items/{id}?action=MINUS", productId)
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(updatedQuantity);
    }

    @Test
    void changeProductQuantityInCart_deleteAction_returnsOkZero() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(cartService.deleteCartItem(userId, productId)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/items/{id}?action=DELETE", productId)
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(0);
    }

}