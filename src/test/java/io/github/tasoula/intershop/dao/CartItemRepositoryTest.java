package io.github.tasoula.intershop.dao;

import static org.junit.jupiter.api.Assertions.*;

import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Optional: If you have a specific test profile
public class CartItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User user;
    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    public void setUp() {
        // Create and persist test data
        user = new User();
        user.setId(UUID.randomUUID()); // Set the ID explicitly
        user = entityManager.persist(user);

        product1 = new Product();
        product1.setId(UUID.randomUUID()); // Set the ID explicitly
        product1.setTitle("Product 1");
        product1.setPrice(BigDecimal.TEN);
        product1 = entityManager.persist(product1);

        product2 = new Product();
        product2.setId(UUID.randomUUID()); // Set the ID explicitly
        product2.setTitle("Product 2");
        product2.setPrice(new BigDecimal("5.50"));
        product2 = entityManager.persist(product2);

        cartItem1 = new CartItem();
        cartItem1.setUser(user);
        cartItem1.setProduct(product1);
        cartItem1.setQuantity(2);
        cartItem1.setCreatedAt(Timestamp.from(Instant.now())); // Set CreatedAt
        cartItem1 = entityManager.persist(cartItem1);

        cartItem2 = new CartItem();
        cartItem2.setUser(user);
        cartItem2.setProduct(product2);
        cartItem2.setQuantity(3);
        cartItem2.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60))); // Slightly older
        cartItem2 = entityManager.persist(cartItem2);

        entityManager.flush();  // Ensure data is written to the database
        entityManager.clear(); // Clear the persistence context to avoid caching issues
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database after each test
        cartItemRepository.deleteAll();
        entityManager.remove(user);  // Remove user before removing products
        entityManager.remove(product1);
        entityManager.remove(product2);
        entityManager.flush();
    }


    @Test
    void findByUserIdAndProductId_ShouldReturnCartItem_WhenExists() {
        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(user.getId(), product1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getProduct().getId()).isEqualTo(product1.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserIdAndProductId_ShouldReturnEmpty_WhenNotExists() {
        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(user.getId(), UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnAllCartItemsForUser() {
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems).extracting(ci -> ci.getProduct().getId()).containsExactlyInAnyOrder(product1.getId(), product2.getId());
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenNoCartItemsForUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        entityManager.persist(otherUser);
        List<CartItem> cartItems = cartItemRepository.findByUserId(otherUser.getId());
        assertThat(cartItems).isEmpty();
        entityManager.remove(otherUser);
        entityManager.flush();

    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnCartItemsOrderedByCreatedAtDescending() {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems.get(0).getProduct().getId()).isEqualTo(product1.getId()); // cartItem1 has the latest CreatedAt
        assertThat(cartItems.get(1).getProduct().getId()).isEqualTo(product2.getId()); // cartItem2 has the oldest CreatedAt
    }

    @Test
    void deleteByUserIdAndProductId_ShouldDeleteCartItem() {
        cartItemRepository.deleteByUserIdAndProductId(user.getId(), product1.getId());
        entityManager.flush(); // Force deletion
        Optional<CartItem> deletedCartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product1.getId());
        assertThat(deletedCartItem).isEmpty();
    }

    @Test
    void calculateTotalPriceByUserId_ShouldReturnCorrectTotalPrice() {
        BigDecimal expectedTotalPrice = product1.getPrice().multiply(BigDecimal.valueOf(cartItem1.getQuantity()))
                .add(product2.getPrice().multiply(BigDecimal.valueOf(cartItem2.getQuantity())));
        BigDecimal totalPrice = cartItemRepository.calculateTotalPriceByUserId(user.getId());
        assertThat(totalPrice).isEqualByComparingTo(expectedTotalPrice);
    }

    @Test
    void calculateTotalPriceByUserId_ShouldReturnZero_WhenNoCartItemsForUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        entityManager.persist(otherUser);
        BigDecimal totalPrice = cartItemRepository.calculateTotalPriceByUserId(otherUser.getId());
        assertThat(totalPrice).isEqualByComparingTo(BigDecimal.ZERO);
        entityManager.remove(otherUser);
        entityManager.flush();
    }

    @Test
    void existsByUserId_ShouldReturnTrue_WhenCartItemsExistForUser() {
        boolean exists = cartItemRepository.existsByUserId(user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_ShouldReturnFalse_WhenNoCartItemsExistForUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        entityManager.persist(otherUser);
        boolean exists = cartItemRepository.existsByUserId(otherUser.getId());
        assertThat(exists).isFalse();
        entityManager.remove(otherUser);
        entityManager.flush();
    }

    @Test
    void deleteByUserId_ShouldDeleteAllCartItemsForUser() {
        cartItemRepository.deleteByUserId(user.getId());
        entityManager.flush(); // Force deletion
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        assertThat(cartItems).isEmpty();
    }
}
