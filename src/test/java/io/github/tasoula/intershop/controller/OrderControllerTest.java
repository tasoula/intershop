package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.OrderService;
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

import java.util.List;
import java.util.UUID;

import static io.github.tasoula.intershop.interceptor.CookieConst.USER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@WebFluxTest(value = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserInterceptor.class))
class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void show_ShouldReturnOrdersViewWithOrders() throws Exception {
        UUID userId = UUID.randomUUID();
        OrderDto order1 = new OrderDto();
        order1.setId(UUID.randomUUID());
        OrderDto order2 = new OrderDto();
        order2.setId(UUID.randomUUID());

        List<OrderDto> orderList = List.of(order1, order2);
        when(orderService.getByUserId(userId)).thenReturn(Flux.fromIterable(orderList));

        webTestClient.get()
                .uri("/orders")
                .cookie(USER_ID, userId.toString())
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

        webTestClient.get()
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
        webTestClient.get()
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
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderService.createOrder(userId)).thenReturn(Mono.just(orderId));

      webTestClient.post()
              .uri("/orders/new")
              .cookie(USER_ID, userId.toString())
              .exchange()
              .expectStatus().is3xxRedirection()
              .expectHeader().valueEquals("Location", "/orders/" + orderId + "?newOrder=true");
    }

     @Test
    void createOrder_ShouldRedirectToCartItemsWhenOrderCreationFails() throws Exception {
        UUID userId = UUID.randomUUID();
        when(orderService.createOrder(userId)).thenReturn(Mono.empty());

         when(orderService.createOrder(userId)).thenReturn(Mono.empty());

         webTestClient.post()
                 .uri("/orders/new")
                 .cookie(USER_ID, userId.toString())
                 .exchange()
                 .expectStatus().is3xxRedirection()
                 .expectHeader().valueEquals("Location", "/cart/items");
    }

}