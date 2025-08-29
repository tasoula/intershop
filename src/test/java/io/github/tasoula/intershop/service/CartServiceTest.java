package io.github.tasoula.intershop.service;


import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CartServiceTest  {

    @InjectMocks
    private CartService cartService;
    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EntityManager entityManager;

    private UUID userId;
    private User user;
    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setStockQuantity(10);
    }

    @Test
    void findByUserId_shouldReturnProductDtoList() {
        // Arrange
        CartItem cartItem1 = new CartItem(user, product);
        cartItem1.setCreatedAt(Timestamp.from(Instant.now()));
        cartItem1.setQuantity(2);

        UUID productId2 = UUID.randomUUID();
        Product product2 = new Product();
        product2.setId(productId2);
        product2.setTitle("Test Product 2");
        product2.setStockQuantity(20);

        CartItem cartItem2 = new CartItem(user, product2);
        cartItem2.setQuantity(3);
        cartItem2.setCreatedAt(Timestamp.from(Instant.MIN));
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);

        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(cartItems);

        // Act
        List<ProductDto> productDtos = cartService.findByUserId(userId);

        // Assert
        assertEquals(2, productDtos.size());
        assertEquals(product.getTitle(), productDtos.get(0).getTitle());
        assertEquals(cartItem1.getQuantity(), productDtos.get(0).getQuantity()); // Проверяем, что количество корректно
        assertEquals(cartItem2.getQuantity(), productDtos.get(1).getQuantity());
        verify(cartItemRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

     @Test
    void changeProductQuantityInCart_productNotFound_shouldThrowException() {
           // Arrange
           when(entityManager.getReference(Product.class, productId)).thenThrow(EntityNotFoundException.class);

           // Act & Assert
           assertThrows(EntityNotFoundException.class, () -> cartService.changeProductQuantityInCart(userId, productId, 1));
           verify(entityManager, times(1)).getReference(Product.class, productId);
           verifyNoInteractions(cartItemRepository);
       }

    @Test
    void changeProductQuantityInCart_cartItemNotFound_shouldCreateNewCartItem() {
        // Arrange
        when(entityManager.getReference(User.class, userId)).thenReturn(user);
        when(entityManager.getReference(Product.class, productId)).thenReturn(product);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        int newQuantity = cartService.changeProductQuantityInCart(userId, productId, 5);

        // Assert
        assertEquals(5, newQuantity);
        verify(entityManager, times(1)).getReference(Product.class, productId);
        verify(entityManager, times(1)).getReference(User.class, userId);
        verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

        @Test
    void changeProductQuantityInCart_changeQuantityIsNegativeAndResultIsZero_shouldDeleteCartItem() {
           // Arrange
           CartItem cartItem = new CartItem(user, product);
           cartItem.setQuantity(2);
           when(entityManager.getReference(Product.class, productId)).thenReturn(product);
           when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));

           // Act
           int newQuantity = cartService.changeProductQuantityInCart(userId, productId, -2);

           // Assert
           assertEquals(0, newQuantity);
           verify(entityManager, times(1)).getReference(Product.class, productId);
           verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
           verify(cartItemRepository, times(1)).delete(cartItem);
           verify(cartItemRepository, never()).save(any(CartItem.class));
       }

      @Test
       void changeProductQuantityInCart_changeQuantityIsPositiveAndResultExceedsStock_shouldSetQuantityToStockQuantity() {
           // Arrange
           Product productWithLimitedStock = new Product();
           productWithLimitedStock.setId(productId);
           productWithLimitedStock.setTitle("Test Product");
           productWithLimitedStock.setStockQuantity(3);

           CartItem cartItem = new CartItem(user, productWithLimitedStock);
           cartItem.setQuantity(2);

           when(entityManager.getReference(Product.class, productId)).thenReturn(productWithLimitedStock);
           when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));
           when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

           // Act
           int newQuantity = cartService.changeProductQuantityInCart(userId, productId, 5);

           // Assert
           assertEquals(3, newQuantity);
           verify(entityManager, times(1)).getReference(Product.class, productId);
           verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
           verify(cartItemRepository, times(1)).save(any(CartItem.class));
       }

         @Test
         void changeProductQuantityInCart_successfulUpdate() {
             // Arrange
             CartItem cartItem = new CartItem(user, product);
             cartItem.setQuantity(2);

             when(entityManager.getReference(Product.class, productId)).thenReturn(product);
             when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));
             when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

             // Act
             int newQuantity = cartService.changeProductQuantityInCart(userId, productId, 3);

             // Assert
             assertEquals(5, newQuantity);
             verify(entityManager, times(1)).getReference(Product.class, productId);
             verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
             verify(cartItemRepository, times(1)).save(cartItem);
         }

    @Test
    void calculateTotalPriceByUserId_ShouldReturnTotalPrice_WhenCartIsNotEmpty() {
        // Arrange
        BigDecimal expectedTotalPrice = new BigDecimal("100.00");
        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(expectedTotalPrice);

        // Act
        BigDecimal actualTotalPrice = cartService.calculateTotalPriceByUserId(userId);

        // Assert
        assertEquals(expectedTotalPrice, actualTotalPrice);
    }

    @Test
    void calculateTotalPriceByUserId_ShouldReturnZero_WhenCartIsEmpty() {
        // Arrange
        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(null);

        // Act
        BigDecimal actualTotalPrice = cartService.calculateTotalPriceByUserId(userId);

        // Assert
        assertEquals(BigDecimal.ZERO, actualTotalPrice);
    }

    @Test
    void isEmpty_ShouldReturnTrue_WhenCartIsEmpty() {
        // Arrange
        when(cartItemRepository.existsByUserId(userId)).thenReturn(false);

        // Act
        boolean isEmpty = cartService.isEmpty(userId);

        // Assert
        assertTrue(isEmpty);
    }

    @Test
    void isEmpty_ShouldReturnFalse_WhenCartIsNotEmpty() {
        // Arrange
        when(cartItemRepository.existsByUserId(userId)).thenReturn(true);

        // Act
        boolean isEmpty = cartService.isEmpty(userId);

        // Assert
        assertFalse(isEmpty);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsQuantityFromCartItemRepository() {
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);

        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));
        int quantity = cartService.getCartQuantity(userId, productId);
        assertEquals(5, quantity);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsZeroIfCartItemNotFound() {
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        int quantity = cartService.getCartQuantity(userId, productId);
        assertEquals(0, quantity);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNull_ReturnsZero() {
        int quantity = cartService.getCartQuantity(null, productId);
        assertEquals(0, quantity);
        verifyNoInteractions(cartItemRepository);
    }

}