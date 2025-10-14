package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.CartService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(CartController.class)
@AutoConfigureWebTestClient
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    private static UUID userId = UUID.randomUUID();
    private static Authentication userAuthentication;

    @BeforeAll
    static void beforeAll() {
        userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUserName("testuser");
        mockUser.setPassword("password");
        mockUser.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        userAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
    }

    @Test
    void viewCart_shouldReturnCartViewWithItemsAndTotal() throws Exception {
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

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/cart/items")
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
        List<ProductDto> items = List.of();
        BigDecimal total = BigDecimal.ZERO;

        when(cartService.findByUserId(userId)).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(total));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/cart/items")
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
        BigDecimal total = BigDecimal.valueOf(42.50);

        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(total));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/cart/total")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(total);
    }

    @Test
    void isEmpty_returnsTrueWhenCartIsEmpty() {
        when(cartService.isEmpty(userId)).thenReturn(Mono.just(true));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/cart/is_empty")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void isEmpty_returnsFalseWhenCartIsNotEmpty() {
        when(cartService.isEmpty(userId)).thenReturn(Mono.just(false));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/cart/is_empty")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void changeProductQuantityInCart_plusAction_returnsUpdatedQuantity() {
        UUID productId = UUID.randomUUID();
        int updatedQuantity = 5;

        when(cartService.changeProductQuantityInCart(userId, productId, 1)).thenReturn(Mono.just(updatedQuantity));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .mutateWith(csrf())
                .post()
                .uri("/cart/items/{id}?action=PLUS", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(updatedQuantity);
    }

    @Test
    void changeProductQuantityInCart_minusAction_returnsUpdatedQuantity() {
        UUID productId = UUID.randomUUID();
        int updatedQuantity = 3;

        when(cartService.changeProductQuantityInCart(userId, productId, -1)).thenReturn(Mono.just(updatedQuantity));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .mutateWith(csrf())
                .post()
                .uri("/cart/items/{id}?action=MINUS", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(updatedQuantity);
    }

    @Test
    void changeProductQuantityInCart_deleteAction_returnsOkZero() {
        UUID productId = UUID.randomUUID();

        when(cartService.deleteCartItem(userId, productId)).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .mutateWith(csrf())
                .post()
                .uri("/cart/items/{id}?action=DELETE", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(0);
    }


    @Test
    void viewCart_accessDeniedForUnauthentificated() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(cartService); // Verify that the service method was never called
    }

    @Test
    void getTotal_accessDeniedForUnauthentificated() {
        webTestClient.get()
                .uri("/cart/total")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(cartService); // Verify that the service method was never called
    }

    @Test
    void isEmpty_accessDeniedForUnauthentificated() {
        webTestClient.get()
                .uri("/cart/is_empty")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(cartService); // Verify that the service method was never called
    }

    @Test
    void isAvailable_accessDeniedForUnauthentificated() {
        webTestClient.get()
                .uri("/cart/is_available")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(cartService); // Verify that the service method was never called
    }

    @Test
    void changeProductQuantityInCart_accessDeniedForUnauthentificated() {
        webTestClient.post()
                .uri("/cart/items/{id}?action=PLUS", UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verifyNoInteractions(cartService); // Verify that the service method was never called
    }
}