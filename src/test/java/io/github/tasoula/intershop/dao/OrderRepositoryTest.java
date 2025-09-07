package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Order;
import io.github.tasoula.intershop.model.User;
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
 /*   @Autowired
    private TestEntityManager entityManager;  // Provides utilities for interacting with the persistence context

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void whenFindByUserId_thenReturnOrders() {
        // Given
        User user1 = entityManager.persist(new User());
        User user2 = entityManager.persist(new User());

        Order order1 = new Order();
        order1.setUser(user1);
        entityManager.persist(order1);

        Order order2 = new Order();
        order2.setUser(user1);
        entityManager.persist(order2);

        Order order3 = new Order();
        order3.setUser(user2);
        entityManager.persist(order3);

        entityManager.flush();  // Ensure all entities are persisted to the database
        entityManager.clear();  // Detach all entities from the persistence context

        // When
        List<Order> foundOrders = orderRepository.findByUserId(user1.getId());

        // Then
        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders.get(0).getUser().getId()).isEqualTo(user1.getId());
        assertThat(foundOrders.get(1).getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    public void whenFindByUserId_thenReturnEmptyList_ifNoOrdersFound() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        List<Order> foundOrders = orderRepository.findByUserId(userId);

        // Then
        assertThat(foundOrders).isEmpty();
    }

    // Assuming Order has fields besides UserId.  Add tests to check those.  Example:
    @Test
    public void whenFindByUserId_thenReturnOrdersWithCorrectDetails() {
        // Given
        User user = entityManager.persist(new User());
        Order order1 = new Order();
        order1.setUser(user);
        order1.setTotalAmount(BigDecimal.valueOf(100.00));
        order1.setCreatedAt(Timestamp.from(Instant.now()));
        entityManager.persist(order1);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Order> foundOrders = orderRepository.findByUserId(user.getId());

        // Then
        assertThat(foundOrders).hasSize(1);
        Order returnedOrder = foundOrders.get(0);
        assertThat(returnedOrder.getUser().getId()).isEqualTo(user.getId());
        assertThat(returnedOrder.getTotalAmount()).isEqualTo(BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP));
        assertThat(returnedOrder.getCreatedAt()).isEqualTo(order1.getCreatedAt()); //Compare dates correctly.  Consider Instant.
    }

  */
}