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
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Order order1;
    private Order order2;
    private UUID userId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        userRepository.deleteAll().block();

        User user =  new User();
        user.setCreatedAt(Timestamp.from(Instant.now()));
        userId = userRepository.save(user).block().getId();

        order1 = new Order();
        order1.setUserId(userId);
        order1.setCreatedAt(Timestamp.from(Instant.now()));
        order1.setTotalAmount(BigDecimal.valueOf(100));

        order2 = new Order();
        order2.setUserId(userId);
        order2.setCreatedAt(Timestamp.from(Instant.now()));
        order2.setTotalAmount(BigDecimal.valueOf(200));
    }

    @Test
    void saveAndFindOrdersByUserId_WhenExists() {
        var orders = orderRepository.saveAll(List.of(order1, order2))
                .thenMany(orderRepository.findByUserId(userId))
                .toIterable();

        assertThat(orders)
                .withFailMessage("Заказы не найдены")
                .isNotEmpty()
                .withFailMessage("неверное количество заказов")
                .hasSize(2);
    }

    @Test
    void saveAndFindOrdersByUserId_WhenNotExists() {
        var orders = orderRepository.saveAll(List.of())
                .thenMany(orderRepository.findByUserId(userId))
                .toIterable();

        assertThat(orders)
                .withFailMessage("Заказы не должны быть найдены")
                .isEmpty();
    }
}