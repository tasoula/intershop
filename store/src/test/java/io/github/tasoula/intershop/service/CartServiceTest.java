package io.github.tasoula.intershop.service;


import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

   @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setStockQuantity(10);
    }

    @Test
    void findByUserId_shouldReturnProductDtoList() {
        // Arrange
        CartItem cartItem1 = new CartItem();
        cartItem1.setUserId(userId);
        cartItem1.setProductId(productId);
        cartItem1.setCreatedAt(Timestamp.from(Instant.now()));
        cartItem1.setQuantity(2);

        UUID productId2 = UUID.randomUUID();
        Product product2 = new Product();
        product2.setId(productId2);
        product2.setTitle("Test Product 2");
        product2.setStockQuantity(20);

        CartItem cartItem2 = new CartItem();
        cartItem2.setUserId(userId);
        cartItem2.setProductId(productId2);
        cartItem2.setQuantity(3);
        cartItem2.setCreatedAt(Timestamp.from(Instant.MIN));
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);

        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Flux.fromIterable(cartItems));
        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(productRepository.findById(product2.getId())).thenReturn(Mono.just(product2));


        StepVerifier.create(cartService.findByUserId(userId))
                .assertNext(productDto -> {
                    assert productDto.getTitle().equals(product.getTitle());
                    assert productDto.getQuantity() == cartItem1.getQuantity();
                })
                .assertNext(productDto -> {
                    assert productDto.getTitle().equals(product2.getTitle());
                    assert productDto.getQuantity() == cartItem2.getQuantity();
                })
                .verifyComplete();

        verify(cartItemRepository).findByUserIdOrderByCreatedAtDesc(userId);
        verify(productRepository).findById(productId);
        verify(productRepository).findById(productId2);
    }

     @Test
    void changeProductQuantityInCart_productNotFound_shouldThrowException() {
         int changeQuantity = 2;

         when(productRepository.findById(productId)).thenReturn(Mono.empty());

         Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

         StepVerifier.create(result)
                 .expectError(ResourceNotFoundException.class)
                 .verify();

         verify(cartItemRepository, never()).save(any(CartItem.class));
         verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
       }

   @Test
    void changeProductQuantityInCart_cartItemNotFound_shouldCreateNewCartItem() {
       int changeQuantity = 3;
       int expectedQuantity = changeQuantity; // Initial quantity equals to changeQuantity because cart item doesn't exist

       when(productRepository.findById(productId)).thenReturn(Mono.just(product));
       when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty());
       when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
           CartItem savedCartItem = invocation.getArgument(0);
           savedCartItem.setQuantity(expectedQuantity); // Simulate the quantity update in the saved item
           return Mono.just(savedCartItem);
       });

       Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

       StepVerifier.create(result)
               .expectNext(expectedQuantity)
               .verifyComplete();

       verify(cartItemRepository, times(1)).save(argThat(item -> item.getQuantity() == expectedQuantity));

       verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);

    }

         @Test
    void changeProductQuantityInCart_changeQuantityIsNegativeAndResultIsZero_shouldDeleteCartItem() {
             // Arrange
             CartItem cartItem = new CartItem();
             cartItem.setUserId(userId);
             cartItem.setProductId(productId);
             cartItem.setQuantity(2);
             int changeQuantity = -5;

             when(productRepository.findById(productId)).thenReturn(Mono.just(product));
             when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
             when(cartItemRepository.deleteByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty()); // Simulate successful delete

             Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

             StepVerifier.create(result)
                     .expectNext(0) // Expect the method to return 0 when deleting
                     .verifyComplete();

             verify(cartItemRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
             verify(cartItemRepository, never()).save(any(CartItem.class)); // Ensure save is not called

         }

      @Test
       void changeProductQuantityInCart_changeQuantityIsPositiveAndResultExceedsStock_shouldSetQuantityToStockQuantity() {
           // Arrange
           Product productWithLimitedStock = new Product();
           productWithLimitedStock.setId(productId);
           productWithLimitedStock.setTitle("Test Product");
           productWithLimitedStock.setStockQuantity(3);

           CartItem cartItem = new CartItem(userId, productWithLimitedStock.getId());
           cartItem.setQuantity(2);

          when(productRepository.findById(productWithLimitedStock.getId())).thenReturn(Mono.just(productWithLimitedStock));
           when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
           when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem));

           // Act
          Mono<Integer> newQuantity = cartService.changeProductQuantityInCart(userId, productId, 5);

          StepVerifier.create(newQuantity)
                  .expectNext(productWithLimitedStock.getStockQuantity())
                  .verifyComplete();

           verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
           verify(cartItemRepository, times(1)).save(any(CartItem.class));
       }

       @Test
        void changeProductQuantityInCart_successfulUpdate() {
            // Arrange
            CartItem cartItem = new CartItem(userId, productId);
            cartItem.setQuantity(2);
           int changeQuantity = 3;
           int expectedQuantity = cartItem.getQuantity() + changeQuantity;

            when(productRepository.findById(productId)).thenReturn(Mono.just(product));
            when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

            // Act
           Mono<Integer> newQuantity = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

           StepVerifier.create(newQuantity)
                   .expectNext(cartItem.getQuantity() + changeQuantity)
                   .verifyComplete();

            // Assert
            verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
            verify(cartItemRepository, times(1)).save(cartItem);
        }

    @Test
   void calculateTotalPriceByUserId_ShouldReturnTotalPrice_WhenCartIsNotEmpty() {
       // Arrange
       BigDecimal expectedTotalPrice = new BigDecimal("100.00");
       when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(expectedTotalPrice));

       // Act
        Mono<BigDecimal> actualTotalPrice = cartService.calculateTotalPriceByUserId(userId);

       // Assert
        StepVerifier.create(actualTotalPrice)
                .expectNext(expectedTotalPrice)
                .verifyComplete();
        verify(cartItemRepository, times(1)).calculateTotalPriceByUserId(userId);
   }

   @Test
   void calculateTotalPriceByUserId_ShouldReturnZero_WhenCartIsEmpty() {
       when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.empty());

       // Act
       Mono<BigDecimal> result = cartService.calculateTotalPriceByUserId(userId);

       // Assert
       StepVerifier.create(result)
               .expectNext(BigDecimal.ZERO)
               .verifyComplete();
       verify(cartItemRepository, times(1)).calculateTotalPriceByUserId(userId);
   }

  @Test
   void isEmpty_ShouldReturnTrue_WhenCartIsEmpty() {
      when(cartItemRepository.existsByUserId(userId)).thenReturn(Mono.just(false));

      // Act
      Mono<Boolean> result = cartService.isEmpty(userId);

      // Assert
      StepVerifier.create(result)
              .expectNext(true)
              .verifyComplete();
      verify(cartItemRepository, times(1)).existsByUserId(userId);
   }

    @Test
   void isEmpty_ShouldReturnFalse_WhenCartIsNotEmpty() {
        // Arrange
        when(cartItemRepository.existsByUserId(userId)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = cartService.isEmpty(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        verify(cartItemRepository, times(1)).existsByUserId(userId);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsQuantityFromCartItemRepository() {
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(5);

        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartService.getCartQuantity(userId, productId))
                .expectNext(cartItem.getQuantity())
                .verifyComplete();

        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);

    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsZeroIfCartItemNotFound() {
        when(cartItemRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(cartService.getCartQuantity(userId, productId))
                .expectNext(0)
                .verifyComplete();

        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNull_ReturnsZero() {
        StepVerifier.create(cartService.getCartQuantity(null, productId))
                .expectNext(0)
                .verifyComplete();

        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void deleteCartItem_ShouldCallRepositoryDeleteMethod() {
        // Arrange
        when(cartItemRepository.deleteByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = cartService.deleteCartItem(userId, productId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(cartItemRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
    }
}