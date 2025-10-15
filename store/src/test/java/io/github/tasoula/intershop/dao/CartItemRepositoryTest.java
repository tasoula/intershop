package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CartItemRepositoryTest extends SpringBootPostgreSQLBase {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository; // Необходимо для создания продуктов
    @Autowired
    private UserRepository userRepository; // Необходимо для создания пользователей

    private UUID userId1;
    private UUID userId2;
    private UUID productId1;
    private UUID productId2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        // Очищаем данные перед каждым тестом
        cartItemRepository.deleteAll().block();
        userRepository.deleteAll().block();

        // Создаем пользователей
        User user1 = new User();
        user1.setCreatedAt(Timestamp.from(Instant.now()));
        user1.setUserName("user1");
        user1.setPassword("user1");
        user1 = userRepository.save(user1).block();
        userId1 = user1.getId();
        userId2 = UUID.randomUUID(); // Создаем второй userId без сохранения в БД

        // Создаем продукты
        Product product1 = new Product();
        product1.setTitle("Product 1");
        product1.setPrice(BigDecimal.valueOf(10.00));
        product1 = productRepository.save(product1).block();
        productId1 = product1.getId();

        Product product2 = new Product();
        product2.setTitle("Product 2");
        product2.setPrice(BigDecimal.valueOf(20.00));
        product2 = productRepository.save(product2).block();
        productId2 = product2.getId();

        // Создаем элементы корзины
        cartItem1 = new CartItem(userId1, productId1);
        cartItem1.setQuantity(2);
        cartItem2 = new CartItem(userId1, productId2);
        cartItem2.setQuantity(1);

    }

     @Test
    void findByUserIdAndProductId_ShouldReturnCartItem_WhenExists() {
         cartItemRepository.save(cartItem1).block();

         CartItem foundItem = cartItemRepository.findByUserIdAndProductId(userId1, productId1).block();

         assertThat(foundItem).isNotNull();
         assertThat(foundItem.getUserId()).isEqualTo(userId1);
         assertThat(foundItem.getProductId()).isEqualTo(productId1);

    }

     @Test
    void findByUserIdAndProductId_ShouldReturnEmpty_WhenNotExists() {
         CartItem foundItem = cartItemRepository.findByUserIdAndProductId(userId1, productId2).block();

         assertThat(foundItem).isNull();
    }

    @Test
    void findByUserId_ShouldReturnAllCartItemsForUser() {
        cartItemRepository.saveAll(List.of(cartItem1, cartItem2)).blockLast();

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId1).collectList().block();

        assertThat(cartItems).isNotNull();
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems.stream().map(CartItem::getUserId).distinct().toList()).containsExactly(userId1);
    }

     @Test
    void findByUserId_ShouldReturnEmptyList_WhenNoCartItemsForUser() {
      List<CartItem> cartItems = cartItemRepository.findByUserId(userId2).collectList().block();
        assertThat(cartItems).isNotNull();
        assertThat(cartItems).isEmpty();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnCartItemsOrderedByCreatedAtDesc() {
        // Создаем элементы корзины с разным временем создания (имитируем разные моменты добавления)
        CartItem item1 = new CartItem(userId1, productId1);
        item1.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(5))); // Старый
        CartItem item2 = new CartItem(userId1, productId2);
        item2.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(1))); // Новый

        cartItemRepository.saveAll(List.of(item1, item2)).blockLast();

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId1).collectList().block();

        assertThat(cartItems).isNotNull();
        assertThat(cartItems).hasSize(2);
        // Проверяем порядок сортировки (последний добавленный должен быть первым)
        assertThat(cartItems.get(0).getCreatedAt()).isAfter(cartItems.get(1).getCreatedAt());
    }


    @Test
    void deleteByUserIdAndProductId_ShouldDeleteCartItem() {
        cartItemRepository.save(cartItem1).block();

        cartItemRepository.deleteByUserIdAndProductId(userId1, productId1).block();

        CartItem deletedItem = cartItemRepository.findByUserIdAndProductId(userId1, productId1).block();
        assertThat(deletedItem).isNull();
    }

    @Test
    void calculateTotalPriceByUserId_ShouldCalculateTotalPrice() {
        cartItemRepository.saveAll(List.of(cartItem1, cartItem2)).blockLast();

        BigDecimal totalPrice = cartItemRepository.calculateTotalPriceByUserId(userId1).block();

        // 2 * 10.00 + 1 * 20.00 = 40.00
        assertThat(totalPrice).isNotNull();
        assertThat(totalPrice).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    void calculateTotalPriceByUserId_ShouldReturnZeroIfNoItems() {
        BigDecimal totalPrice = cartItemRepository.calculateTotalPriceByUserId(userId2).block(); // userId2 не имеет элементов корзины

        assertThat(totalPrice).isNull();
    }

    @Test
    void existsByUserId_ShouldReturnTrueIfUserHasItems() {
        cartItemRepository.save(cartItem1).block();

        boolean exists = cartItemRepository.existsByUserId(userId1).block();

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_ShouldReturnFalseIfUserHasNoItems() {
        boolean exists = cartItemRepository.existsByUserId(userId2).block();

        assertThat(exists).isFalse();
    }

    @Test
    void deleteByUserId_ShouldDeleteAllCartItemsForUser() {
        cartItemRepository.saveAll(List.of(cartItem1, cartItem2)).blockLast();

        cartItemRepository.deleteByUserId(userId1).block();

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId1).collectList().block();
        assertThat(cartItems).isNotNull();
        assertThat(cartItems).isEmpty();
    }
}
