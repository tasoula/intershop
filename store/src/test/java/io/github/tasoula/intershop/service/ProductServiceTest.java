package io.github.tasoula.intershop.service;


import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
 @ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductDataService productDataService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ProductService productService;

    private UUID userId;
    private UUID productId;
    private Product product;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(10);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAll_shouldReturnPageOfProductDto() {
        // Arrange
        String search = "Test";
        when(productDataService.findAll(search, pageable)).thenReturn(Mono.just(List.of(product)));
        when(productDataService.count()).thenReturn(Mono.just(1L));
        when(cartService.getCartQuantity(userId, productId)).thenReturn(Mono.just(2));

        // Act
        Mono<Page<ProductDto>> result = productService.findAll(userId, search, pageable);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals("Test Product", page.getContent().get(0).getTitle());
                    assertEquals(2, page.getContent().get(0).getQuantity());
                    assertEquals(1, page.getTotalElements());
                })
                .verifyComplete();

        verify(productDataService).findAll(search, pageable);
        verify(productDataService).count();
        verify(cartService).getCartQuantity(userId, productId);
    }

    @Test
    void findById_shouldReturnProductDto() {
        // Arrange
        when(productDataService.findById(productId)).thenReturn(Mono.just(product));
        when(cartService.getCartQuantity(userId, productId)).thenReturn(Mono.just(2));

        // Act
        Mono<ProductDto> result = productService.findById(userId, productId);

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals("Test Product", dto.getTitle());
                    assertEquals(2, dto.getQuantity());
                })
                .verifyComplete();

        verify(productDataService).findById(productId);
        verify(cartService).getCartQuantity(userId, productId);
    }

    @Test
    void findById_shouldReturnError_whenProductNotFound() {
        // Arrange
        when(productDataService.findById(productId)).thenReturn(Mono.empty());

        // Act
        Mono<ProductDto> result = productService.findById(userId, productId);

        // Assert
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(productDataService).findById(productId);
        verifyNoInteractions(cartService);
    }

    @Test
    void createProduct_shouldCreateProductSuccessfully() {
        // Arrange
        FilePart image = mock(FilePart.class);
        when(image.filename()).thenReturn("test.jpg");
        when(productDataService.createProduct(any(Product.class), eq(image))).thenReturn(Mono.empty());
        ProductDto newProductDto = new ProductDto();
        newProductDto.setTitle("New Product");
        newProductDto.setDescription("New Description");
        newProductDto.setPrice(BigDecimal.valueOf(20));
        newProductDto.setStockQuantity(20);

        // Act
        Mono<Void> result = productService.createProduct(newProductDto, image);

        // Assert
        StepVerifier.create(result).verifyComplete();

        verify(productDataService).createProduct(any(Product.class), eq(image));
    }

    @Test
    void createProduct_verifiesFieldsAreCopiedCorrectly() {
        // Arrange
        FilePart image = mock(FilePart.class);
        when(image.filename()).thenReturn("test.jpg");
        when(productDataService.createProduct(any(Product.class), eq(image))).thenReturn(Mono.empty());
        ProductDto newProductDto = new ProductDto();
        newProductDto.setTitle("New Product");
        newProductDto.setDescription("New Description");
        newProductDto.setPrice(BigDecimal.valueOf(20));
        newProductDto.setStockQuantity(20);

        // Act
        productService.createProduct(newProductDto, image).subscribe(); // Subscribe to trigger the method call

        // Assert
        verify(productDataService).createProduct(argThat(product ->
                        product.getTitle().equals("New Product") &&
                                product.getDescription().equals("New Description") &&
                                product.getPrice().equals(BigDecimal.valueOf(20)) &&
                                product.getStockQuantity() == 20 &&
                                product.getImgPath().startsWith("")),
                eq(image));
    }

}

 */

