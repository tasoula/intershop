package io.github.tasoula.intershop.service;


import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ProductService productService;

    private UUID userId;
    private UUID productId;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(10.0));
        product.setStockQuantity(100);

        cartItem = new CartItem();
        cartItem.setUser(new User(userId));
        cartItem.setProduct(new Product(productId));
        cartItem.setQuantity(5);
    }

    @Test
    void findAll_WithSearchTerm_ReturnsProductDtoPage() {
        String search = "test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable))
                .thenReturn(productPage);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));

        Page<ProductDto> result = productService.findAll(userId, search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(productRepository).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void findAll_WithoutSearchTerm_ReturnsProductDtoPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAllByStockQuantityGreaterThan(0, pageable)).thenReturn(productPage);
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));

        Page<ProductDto> result = productService.findAll(userId, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(productRepository).findAllByStockQuantityGreaterThan(0, pageable);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void findById_ProductExists_ReturnsProductDto() throws ResourceNotFoundException {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));

        ProductDto result = productService.findById(userId, productId);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        verify(productRepository).findById(productId);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void findById_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(userId, productId));
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsQuantityFromCartItemRepository() {
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(cartItem));
        int quantity = productService.getCartQuantity(userId, productId);
        assertEquals(5, quantity);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsZeroIfCartItemNotFound() {
        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());
        int quantity = productService.getCartQuantity(userId, productId);
        assertEquals(0, quantity);
        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNull_ReturnsZero() {
        int quantity = productService.getCartQuantity(null, productId);
        assertEquals(0, quantity);
        verifyNoInteractions(cartItemRepository);
    }
}
