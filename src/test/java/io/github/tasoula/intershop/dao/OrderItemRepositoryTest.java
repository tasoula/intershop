package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemRepositoryTest extends SpringBootPostgreSQLBase{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    private UUID orderId;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        productRepository.deleteAll().block();
        userRepository.deleteAll().block();

        User user =  new User();
        user.setCreatedAt(Timestamp.from(Instant.now()));
        UUID userId = userRepository.save(user).block().getId();

        Product product1 = new Product();
        product1.setTitle("Amazing Product");
        product1.setDescription("This is an amazing product description.");
        product1.setPrice(new BigDecimal("25.00"));
        product1.setStockQuantity(10);
        product1.setImgPath("/images/product1.jpg");
        UUID productId1 = productRepository.save(product1).block().getId();

        Product product2 = new Product();
        product2.setTitle("Another Product");
        product2.setDescription("Another great product.");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStockQuantity(5);
        UUID productId2 = productRepository.save(product2).block().getId();

        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(Timestamp.from(Instant.now()));
        order.setTotalAmount(BigDecimal.valueOf(100));
        orderId = orderRepository.save(order).block().getId();

        orderItem1 = new OrderItem();
        orderItem1.setOrderId(orderId);
        orderItem1.setProductId(productId1);
        orderItem1.setQuantity(5);
        orderItem1.setPriceAtTimeOfOrder(BigDecimal.TEN);

        orderItem2 = new OrderItem();
        orderItem2.setOrderId(orderId);
        orderItem2.setProductId(productId2);
        orderItem2.setQuantity(5);
        orderItem2.setPriceAtTimeOfOrder(BigDecimal.valueOf(101));
    }

    @Test
    void saveAndFindOrdersByUserId_WhenExists() {
        var orderItems = orderItemRepository.saveAll(List.of(orderItem1, orderItem2))
                .thenMany(orderItemRepository.findByOrderId(orderId))
                .toIterable();

        assertThat(orderItems)
                .withFailMessage("Позиции заказа не найдены")
                .isNotEmpty()
                .withFailMessage("неверное количество позиций в заказе")
                .hasSize(2)
                 .first()
                .withFailMessage("первая позиция должна быть: " + orderItem1.getId())
                .extracting(OrderItem::getId)
                .isEqualTo(orderItem1.getId());
    }

    @Test
    void saveAndFindOrdersByUserId_WhenNotExists() {
        var orders = orderItemRepository.findByOrderId(UUID.randomUUID()).toIterable();

        assertThat(orders)
                .withFailMessage("Заказы не должны быть найдены")
                .isEmpty();
    }
}