package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    private static UUID userId;
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
    void show_ShouldReturnOrdersViewWithOrders() throws Exception {
        OrderDto order1 = new OrderDto();
        order1.setId(UUID.randomUUID());
        OrderDto order2 = new OrderDto();
        order2.setId(UUID.randomUUID());

        List<OrderDto> orderList = List.of(order1, order2);
        when(orderService.getByUserId(userId)).thenReturn(Flux.fromIterable(orderList));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());

                    assertTrue(body.contains("<title>Заказы</title>"), "page title not found in template");
                    assertTrue(body.contains(String.format("<a href=\"/orders/%s\">Заказ №%s</a>", order1.getId(), order1.getId())), "order1 not found in template");
                    assertTrue(body.contains(String.format("<a href=\"/orders/%s\">Заказ №%s</a>", order2.getId(), order2.getId())), "order2 not found in template");
                });
    }

    @Test
    void showOrder_ShouldReturnOrderViewWithOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderDto order = new OrderDto(); // Mock Order object
        order.setId(orderId);

        when(orderService.getById(orderId)).thenReturn(Mono.just(order));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());

                    assertTrue(body.contains("<title>Заказ</title>"), "page title not found in template");
                    assertTrue(body.contains(String.format("<h2>Заказ №%s</h2>", order.getId())), "order not found in template");
                });
    }

    @Test
    void showOrder_returnsOrderView_withNewOrderFlag() {
        UUID orderId = UUID.randomUUID();
        OrderDto order = new OrderDto();
        order.setId(orderId);
        when(orderService.getById(orderId)).thenReturn(Mono.just(order));
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/orders/" + orderId + "?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());

                    assertTrue(body.contains("<title>Заказ</title>"), "page title not found in template");
                    assertTrue(body.contains(String.format("<h2>Заказ №%s</h2>", order.getId())), "order not found in template");
                });
    }


  @Test
    void createOrder_ShouldRedirectToOrderWithNewOrderTrue() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.createOrder(userId)).thenReturn(Mono.just(orderId));

      webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
              .mutateWith(csrf())
              .post()
              .uri("/orders/new")
              .exchange()
              .expectStatus().is3xxRedirection()
              .expectHeader().valueEquals("Location", "/orders/" + orderId + "?newOrder=true");
    }

     @Test
    void createOrder_ShouldRedirectToCartItemsWhenOrderCreationFails() throws Exception {
        when(orderService.createOrder(userId)).thenReturn(Mono.empty());

         when(orderService.createOrder(userId)).thenReturn(Mono.empty());

         webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                 .mutateWith(csrf())
                 .post()
                 .uri("/orders/new")
                 .exchange()
                 .expectStatus().is3xxRedirection()
                 .expectHeader().valueEquals("Location", "/cart/items");
    }

    @Test
    void show_redirectForUnauthentificated() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(orderService); // Verify that the service method was never called
    }

    @Test
    void showOrder_redirectForUnauthentificated() {
        webTestClient.get()
                .uri("/orders/"+UUID.randomUUID())
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login"); // Expect redirect

        Mockito.verifyNoInteractions(orderService); // Verify that the service method was never called
    }

    @Test
    void changeProductQuantityInCart_createOrder() {
        webTestClient.post()
                .uri("/orders/new")
                .exchange()
                .expectStatus().isForbidden();

        Mockito.verifyNoInteractions(orderService); // Verify that the service method was never called
    }
}