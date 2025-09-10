package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Order;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
class OrderRepositoryTest extends SpringBootPostgreSQLBase{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId1;
    private UUID userId2;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        userRepository.deleteAll().block();

        User user1 =  new User();
        user1.setCreatedAt(Timestamp.from(Instant.now()));
        userId1 = userRepository.save(user1).block().getId();

        userId2 = UUID.randomUUID();

        order1 = new Order();
        order1.setUserId(userId1);
        order1.setCreatedAt(Timestamp.from(Instant.now()));
        order1.setTotalAmount(BigDecimal.valueOf(100.50));

        order2 = new Order();
        order2.setUserId(userId1);
        order2.setCreatedAt(Timestamp.from(Instant.now()));
        order2.setTotalAmount(BigDecimal.valueOf(250.75));
    }

    @Test
    void whenFindByUserId_thenReturnOrders() {
        orderRepository.saveAll(List.of(order1, order2)).blockLast();

        List<Order> orders = orderRepository.findByUserId(userId1).collectList().block();

        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getUserId).allMatch(id -> id.equals(userId1));
    }

    @Test
    void whenFindByUserId_thenReturnEmptyList_ifNoOrdersFound() {
        orderRepository.save(order1).block();

        // Запрос для userId2, у которого нет заказов
        List<Order> orders = orderRepository.findByUserId(userId2).collectList().block();

        assertThat(orders).isEmpty();
    }

    @Test
    void whenFindByUserId_thenReturnOrdersWithCorrectDetails() {
        orderRepository.save(order1).block();

        Order savedOrder = orderRepository.findByUserId(userId1).blockFirst();

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(order1.getId());
        assertThat(savedOrder.getUserId()).isEqualTo(userId1);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(order1.getTotalAmount());
    }
}