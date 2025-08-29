package io.github.tasoula.intershop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.model.*;
import io.github.tasoula.intershop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID orderId;
    private Product product;
    private CartItem cartItem;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setTitle("Test Product");
        product.setPrice(BigDecimal.valueOf(10));
        product.setStockQuantity(100);

        cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        cartItem.setUser(new User(userId));
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        order = new Order();
        order.setId(orderId);
        order.setUser(new User(userId));
        order.setTotalAmount(BigDecimal.valueOf(20)); // 10 * 2

        orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setPriceAtTimeOfOrder(product.getPrice());
        orderItem.setQuantity(2);
        order.setOrderItems(List.of(orderItem));

    }

    @Test
    void createOrder_ShouldReturnOrderId_WhenCartIsNotEmptyAndStockIsSufficient() {
        // Arrange
        when(cartItemRepository.findByUserId(userId)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Optional<UUID> createdOrderId = orderService.createOrder(userId);

        // Assert
        assertTrue(createdOrderId.isPresent());
        assertEquals(orderId, createdOrderId.get());
        verify(cartItemRepository, times(1)).deleteByUserId(userId);
        assertEquals(98, product.getStockQuantity());
    }

    @Test
    void createOrder_ShouldReturnEmptyOptional_WhenCartIsEmpty() {        // Arrange
        when(cartItemRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        Optional<UUID> createdOrderId = orderService.createOrder(userId);

        // Assert
        assertTrue(createdOrderId.isEmpty());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartItemRepository, never()).deleteByUserId(userId);
    }

    @Test
    void createOrder_ShouldReturnEmptyOptional_WhenStockIsInsufficient() {
        // Arrange
        product.setStockQuantity(1); //set stock less than cart quantity
        when(cartItemRepository.findByUserId(userId)).thenReturn(List.of(cartItem));

        // Act
        Optional<UUID> createdOrderId = orderService.createOrder(userId);

        // Assert
        assertTrue(createdOrderId.isEmpty());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartItemRepository, never()).deleteByUserId(userId);
    }

    @Test
    void getById_ShouldReturnOrderDto_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<OrderDto> orderDto = orderService.getById(orderId);

        // Assert
        assertTrue(orderDto.isPresent());
        assertEquals(orderId, orderDto.get().getId());
        assertEquals(BigDecimal.valueOf(20), orderDto.get().getTotalAmount());
        assertEquals(1, orderDto.get().getItems().size());
    }

    @Test
    void getById_ShouldReturnEmptyOptional_WhenOrderDoesNotExist() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<OrderDto> orderDto = orderService.getById(orderId);

        // Assert
        assertTrue(orderDto.isEmpty());
    }

    @Test
    void getByUserId_ShouldReturnListOfOrderDto_WhenOrdersExistForUser() {
        // Arrange
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));

        // Act
        List<OrderDto> orderDtos = orderService.getByUserId(userId);

        // Assert
        assertEquals(1, orderDtos.size());
        assertEquals(orderId, orderDtos.get(0).getId());
    }

    @Test
    void getByUserId_ShouldReturnEmptyList_WhenNoOrdersExistForUser() {
        // Arrange
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<OrderDto> orderDtos = orderService.getByUserId(userId);

        // Assert
        assertTrue(orderDtos.isEmpty());
    }
}