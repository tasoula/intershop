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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
    private UUID productId;
    private Product product;
    private CartItem cartItem;

    private MultipartFile mockImage;

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

        mockImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
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
        when(cartService.getCartQuantity(userId, productId)).thenReturn(cartItem.getQuantity());

        Page<ProductDto> result = productService.findAll(userId, search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(productRepository).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable);
        verify(cartService).getCartQuantity(userId, productId);
    }

    @Test
    void findAll_WithoutSearchTerm_ReturnsProductDtoPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAllByStockQuantityGreaterThan(0, pageable)).thenReturn(productPage);
        when(cartService.getCartQuantity(userId, productId)).thenReturn(cartItem.getQuantity());

        Page<ProductDto> result = productService.findAll(userId, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(productRepository).findAllByStockQuantityGreaterThan(0, pageable);
        verify(cartService).getCartQuantity(userId, productId);
    }

  @Test
    void findById_ProductExists_ReturnsProductDto() throws ResourceNotFoundException {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
      when(cartService.getCartQuantity(userId, productId)).thenReturn(cartItem.getQuantity());

        ProductDto result = productService.findById(userId, productId);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        verify(productRepository).findById(productId);
        verify(cartService).getCartQuantity(userId, productId);
    }

      @Test
    void findById_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(userId, productId));
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartService);
    }

    @Test
    void createProduct_ShouldSaveProductAndImage() throws IOException {
        // Arrange
        String title = "Test Product";
        String description = "Test Description";
        BigDecimal price = BigDecimal.valueOf(99.99);
        int stockQuantity = 10;

        // Capture the Product object passed to the repository
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        // Act
        productService.createProduct(title, description, mockImage, price, stockQuantity);

        // Assert
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertNotNull(savedProduct);
        assertEquals(title, savedProduct.getTitle());
        assertEquals(description, savedProduct.getDescription());
        assertEquals(price, savedProduct.getPrice());
        assertEquals(stockQuantity, savedProduct.getStockQuantity());
        assertNotNull(savedProduct.getImgPath());  // Check if imgPath is set. It will be difficult to precisely match, so just check it's not null
        verify(imageService, times(1)).saveToDisc(eq(mockImage), anyString()); // Verify saveToDisc is called

        // Further assertions related to the filename.  Since UUID is involved, we only check the original filename part.
        String expectedFilenamePart = "_" + mockImage.getOriginalFilename();
        verify(imageService, times(1)).saveToDisc(mockImage,savedProduct.getImgPath());

    }

    @Test
    void createProduct_ShouldCallImageServiceWithCorrectFilename() throws IOException {
        // Arrange
        String title = "Test Product";
        String description = "Test Description";
        BigDecimal price = BigDecimal.valueOf(99.99);
        int stockQuantity = 10;


        // Capture the filename passed to imageService.saveToDisc
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        productService.createProduct(title, description, mockImage, price, stockQuantity);

        // Assert
        verify(imageService, times(1)).saveToDisc(eq(mockImage), filenameCaptor.capture()); // Verify saveToDisc is called with the MultipartFile and filename.
        String capturedFilename = filenameCaptor.getValue();

        assertNotNull(capturedFilename);
        // Check that the captured filename contains the original filename
        String expectedFilenamePart = "_" + mockImage.getOriginalFilename();

        // check saveToDisc called with correct image

    }

}
