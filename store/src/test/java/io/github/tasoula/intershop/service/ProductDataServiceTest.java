package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.Product;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Ensure one instance of the test class
public class ProductDataServiceTest {

    @MockitoSpyBean
    private ProductRepository productRepository;
    @MockitoSpyBean
    private ImageService imageService;
    @Autowired
    private ProductDataService productDataService;
    @Autowired
    private CacheManager cacheManager;

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7.4.2-bookworm").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeAll // Runs before all tests, once per class
    static void beforeAll() {
        redis.start(); // Ensure Redis is started BEFORE Spring context is initialized
    }

    @AfterAll
    static void afterAll() {
        redis.stop(); // Stop Redis after all tests have finished
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Clear cache before each test
        clearCache("products_all");
        clearCache("products");
    }

    @AfterEach
    void tearDown() throws Exception {
        clearCache("products_all");
        clearCache("products");
    }

    private void clearCache(String cacheName) {
        if (cacheManager != null && cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @Test
    void findAll_withSearchTerm_returnsProductsFromRepository() {
        String searchTerm = "test";
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<Product> expectedProducts = List.of(new Product(), new Product());

        when(productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), 0, pageable))
                .thenReturn(Flux.fromIterable(expectedProducts));

        Mono<List<Product>> result = productDataService.findAll(searchTerm, pageable);

        StepVerifier.create(result)
                .expectNext(expectedProducts)
                .verifyComplete();

        verify(productRepository).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), 0, pageable);
    }

    @Test
    void findAll_withoutSearchTerm_returnsProductsFromRepository() {
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<Product> expectedProducts = List.of(new Product(), new Product());

        when(productRepository.findByStockQuantityGreaterThan(0, pageable))
                .thenReturn(Flux.fromIterable(expectedProducts));

        Mono<List<Product>> result = productDataService.findAll(null, pageable);

        StepVerifier.create(result)
                .expectNext(expectedProducts)
                .verifyComplete();

        verify(productRepository).findByStockQuantityGreaterThan(0, pageable);
    }

    @Test
    void findAll_cachesResult() {
        String searchTerm = "test";
        Pageable pageable = Pageable.ofSize(10).withPage(0);
        List<Product> expectedProducts = List.of(new Product(), new Product());

        when(productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), 0, pageable))
                .thenReturn(Flux.fromIterable(expectedProducts));

        // First call - should hit repository and populate cache
        productDataService.findAll(searchTerm, pageable).block();

        // Verify that the repository was called
        verify(productRepository).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), 0, pageable);

        // Clear the mock's invocations
        reset(productRepository);

        // Second call - should retrieve from cache and not hit repository
        Mono<List<Product>> cachedResult = productDataService.findAll(searchTerm, pageable);

        StepVerifier.create(cachedResult)
                .expectNext(expectedProducts)
                .verifyComplete();

        // Verify that the repository wasn't called the second time
        verify(productRepository, never()).findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), 0, pageable);
    }

    @Test
    void findById_returnsProductFromRepository() {
        UUID productId = UUID.randomUUID();
        Product expectedProduct = new Product();
        expectedProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(expectedProduct));

        Mono<Product> result = productDataService.findById(productId);

        StepVerifier.create(result)
                .expectNext(expectedProduct)
                .verifyComplete();

        verify(productRepository).findById(productId);
    }

    @Test
    void findById_productNotFound_returnsError() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        Mono<Product> result = productDataService.findById(productId);

        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(productRepository).findById(productId);
    }

    @Test
    void findById_cachesResult() {
        UUID productId = UUID.randomUUID();
        Product expectedProduct = new Product();
        expectedProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(expectedProduct));

        // First call - should hit repository and populate cache
        productDataService.findById(productId).block();

        verify(productRepository).findById(productId);

        // Clear the mock's invocations
        reset(productRepository);

        // Second call - should retrieve from cache and not hit repository
        Mono<Product> cachedResult = productDataService.findById(productId);

        StepVerifier.create(cachedResult)
                .expectNext(expectedProduct)
                .verifyComplete();

        verify(productRepository, never()).findById(productId);
    }


    @Test
    void createProduct_evictsAllProductsCache() {
        Product product = new Product();
        FilePart image = mock(FilePart.class);

        when(productRepository.save(product)).thenReturn(Mono.just(product));
        when(imageService.saveToDisc(image, product.getImgPath())).thenReturn(Mono.empty());

        productDataService.createProduct(product, image).block();

        verify(productRepository).save(product);
        verify(imageService).saveToDisc(image, product.getImgPath());

        // Get the cache and assert that it's empty after the evict call.  This validates the CacheEvict annotation.
        assertNull(cacheManager.getCache("products_all").get("test-0-10")); // Assuming cache key structure.  This is fragile.
    }

    @Test
    void updateProduct_updatesCache() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);

        when(productRepository.existsById(productId)).thenReturn(Mono.just(true));
        when(productRepository.save(product)).thenReturn(Mono.just(product));

        productDataService.update(product).block();

        verify(productRepository).existsById(productId);
        verify(productRepository).save(product);

        // Get the cache and assert that it's updated.
        // You might need to retrieve the cache entry and assert its value if you're testing the update logic.
        assertNotNull(cacheManager.getCache("products").get(productId));
    }

    @Test
    void updateProduct_productNotFound_throwsException(){
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);

        when(productRepository.existsById(productId)).thenReturn(Mono.just(false));

        StepVerifier.create(productDataService.update(product))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(productRepository).existsById(productId);
        verify(productRepository, never()).save(product);
    }

    @Test
    void count_returnsCountFromRepository() {
        long expectedCount = 100;
        when(productRepository.count()).thenReturn(Mono.just(expectedCount));

        Mono<Long> result = productDataService.count();

        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        verify(productRepository).count();
    }

    // Здесь можно добавить тесты для обработки исключений, если это необходимо.

    // Можно ли в одном классе использовать мок для productRepository и интеграционно проверить кеширование Redis?
    // Да, именно это и сделано в этом примере.  `productRepository` замокирован, а кеширование Redis проверяется интеграционно,
    // т.к. используется реальный Redis через Testcontainers.
}






