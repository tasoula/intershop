package io.github.tasoula.intershop.service;


import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.dao.ProductRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartService cartService;
    @Mock
    private ImageService imageService;
    @InjectMocks
    private ProductService productService;

    private UUID userId;
    private User user;
    private UUID productId;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(10.0));
        product.setStockQuantity(100);

        cartItem = new CartItem();
        cartItem.setUserId(user.getId());
        cartItem.setProductId(product.getId());
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
                .thenReturn(Flux.fromIterable(products));
        when(cartService.getCartQuantity(userId, productId)).thenReturn(Mono.just(cartItem.getQuantity()));
        when(productRepository.count()).thenReturn(Mono.just((long) products.size()));

        Mono<Page<ProductDto>> result = productService.findAll(userId, search, pageable);

        StepVerifier.create(result)
                .assertNext(page -> {
                    List<ProductDto> content = page.getContent();
                    assert content.size() == products.size();
                    ProductDto dto = content.getFirst();
                    assert dto.getTitle().equals(products.getFirst().getTitle());
                    assert dto.getQuantity() == cartItem.getQuantity();
                })
                .verifyComplete();

        verify(productRepository).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                eq(search.toLowerCase()), eq(search.toLowerCase()), eq(0), any(Pageable.class));
        verify(cartService).getCartQuantity(userId, products.getFirst().getId());
        verify(productRepository).count();
    }

    @Test
    void findAll_WithoutSearchTerm_ReturnsProductDtoPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByStockQuantityGreaterThan(0, pageable)).thenReturn(Flux.fromIterable(products));
        when(cartService.getCartQuantity(userId, productId)).thenReturn(Mono.just(cartItem.getQuantity()));
        when(productRepository.count()).thenReturn(Mono.just((long) products.size()));

        Mono<Page<ProductDto>> result = productService.findAll(userId, null, pageable);

        StepVerifier.create(result)
                .assertNext(page -> {
                    List<ProductDto> content = page.getContent();
                    assert content.size() == products.size();
                    ProductDto dto = content.getFirst();
                    assert dto.getTitle().equals(products.getFirst().getTitle());
                    assert dto.getQuantity() == cartItem.getQuantity();
                })
                .verifyComplete();

        verify(productRepository).findByStockQuantityGreaterThan(0, pageable);
        verify(cartService).getCartQuantity(userId, products.getFirst().getId());
        verify(productRepository).count();
    }

 @Test
    void findById_ProductExists_ReturnsProductDto() throws ResourceNotFoundException {
        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(cartService.getCartQuantity(userId, productId)).thenReturn(Mono.just(cartItem.getQuantity()));

        Mono<ProductDto> result = productService.findById(userId, productId);


     StepVerifier.create(result)
             .assertNext(dto -> {
                 assert dto.getId().equals(productId);
                 assert dto.getTitle().equals(product.getTitle());
                 assert dto.getQuantity() == cartItem.getQuantity();
             })
             .verifyComplete();

        verify(productRepository).findById(productId);
        verify(cartService).getCartQuantity(userId, productId);
    }

       @Test
    void findById_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        Mono<ProductDto> result = productService.findById(userId, productId);

           StepVerifier.create(result)
                   .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException &&
                           throwable.getMessage().contains(productId.toString()))
                   .verify();
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartService);
    }

    @Test
    void createProduct_savesProductAndImage() {
        ProductDto productDto = new ProductDto();
        productDto.setTitle("New Product");
        productDto.setDescription("Description");
        productDto.setPrice(BigDecimal.valueOf(100));
        productDto.setStockQuantity(10);

        FilePart image = mock(FilePart.class);
        when(image.filename()).thenReturn("image.png");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        Product savedProduct = new Product();
        savedProduct.setId(UUID.randomUUID());
        savedProduct.setTitle(productDto.getTitle());
        savedProduct.setDescription(productDto.getDescription());
        savedProduct.setPrice(productDto.getPrice());
        savedProduct.setStockQuantity(productDto.getStockQuantity());
        savedProduct.setImgPath("some_filename.png");

        // Мокаем сохранение продукта, возвращаем savedProduct
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(savedProduct.getId()); // эмулируем установку id после сохранения
            return Mono.just(p);
        });

        // Мокаем сохранение изображения
        when(imageService.saveToDisc(any(FilePart.class), anyString())).thenReturn(Mono.empty());

        Mono<Void> result = productService.createProduct(productDto, image);

        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository).save(productCaptor.capture());
        Product productToSave = productCaptor.getValue();

        // Проверяем, что поля установлены
        assert productToSave.getTitle().equals(productDto.getTitle());
        assert productToSave.getDescription().equals(productDto.getDescription());
        assert productToSave.getPrice().equals(productDto.getPrice());
        assert productToSave.getStockQuantity() == productDto.getStockQuantity();
        assert productToSave.getImgPath() != null && productToSave.getImgPath().contains("image.png");

        verify(imageService).saveToDisc(eq(image), eq(productToSave.getImgPath()));
    }

}
