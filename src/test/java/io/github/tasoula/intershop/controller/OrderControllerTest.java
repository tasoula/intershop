package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.config.WebConfig;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class OrderControllerTest {
 /*   @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    UserInterceptor userInterceptor;

    @Test
    void show_ShouldReturnOrdersViewWithOrders() throws Exception {
        UUID userId = UUID.randomUUID();
        List<OrderDto> orders = Collections.singletonList(new OrderDto()); // Mock Order object
        when(orderService.getByUserId(userId)).thenReturn(orders);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("orders.html"))
                .andExpect(model().attribute("orders", orders));

        Mockito.verify(orderService).getByUserId(userId);
    }

    @Test
    void showOrder_ShouldReturnOrderViewWithOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderDto order = new OrderDto(); // Mock Order object
        when(orderService.getById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", orderId)
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order.html"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", true));

        Mockito.verify(orderService).getById(orderId);
    }

    @Test
    void createOrder_ShouldRedirectToOrderWithNewOrderTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.createOrder(userId)).thenReturn(Optional.of(orderId));

        mockMvc.perform(MockMvcRequestBuilders.post("/orders/new")
                        .param("userId", userId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderId + "?newOrder=true"));

        Mockito.verify(orderService).createOrder(userId);
    }

    @Test
    void createOrder_ShouldRedirectToCartItemsWhenOrderCreationFails() throws Exception {
        UUID userId = UUID.randomUUID();
        when(orderService.createOrder(userId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/orders/new")
                        .param("userId", userId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        Mockito.verify(orderService).createOrder(userId);
    }
  */
}