package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.Product;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProductDataService {
    private final ProductRepository productRepository;
    private final ImageService imageService;

    private final ReactiveRedisTemplate<String, Product> reactiveRedisTemplate; // Inject ReactiveRedisTemplate
    private final String ALL_PRODUCTS_CACHE_KEY_PREFIX = "products_all";

    private final String PRODUCTS_CACHE_KEY_PREFIX = "products";

    public ProductDataService(ProductRepository repository, ImageService imageService, ReactiveRedisTemplate<String, Product> reactiveRedisTemplate) {
        this.productRepository = repository;
        this.imageService = imageService;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Flux<Product> findAll(String search, Pageable pageable) {
        String cacheKey = ALL_PRODUCTS_CACHE_KEY_PREFIX + "-" + search + "-" + pageable.getPageNumber() + "-" + pageable.getPageSize();

        return reactiveRedisTemplate.opsForList().range(cacheKey, 0, -1)
                .hasElements() // Check if the Redis list has any elements
                .flatMapMany(hasElements -> {
                    if (hasElements) {
                        return reactiveRedisTemplate.opsForList().range(cacheKey, 0, -1); // Return cached data
                    } else {
                        return getFromRepository(search, pageable)
                                .collectList()
                                .flatMapMany(products -> {
                                    reactiveRedisTemplate.opsForList().rightPushAll(cacheKey, products).subscribe();
                                    reactiveRedisTemplate.expire(cacheKey, Duration.ofMinutes(3)).subscribe();
                                    return Flux.fromIterable(products);
                                });
                    }
                });
    }

    @Cacheable(value = PRODUCTS_CACHE_KEY_PREFIX, key = "#productId")
    public Mono<Product> findById(UUID productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product not found with id: " + productId)));
    }

    public Mono<Long> count(){
        return productRepository.count();
    }

    @Transactional
    public Mono<Void> createProduct(Product product, FilePart image) {
        return productRepository.save(product)
                .flatMap(savedProduct -> imageService.saveToDisc(image, product.getImgPath())
                        .thenReturn(savedProduct))
                .then(clearAllProductsCache()); // Очищаем кеш после создания продукта
    }

    @CachePut(value = PRODUCTS_CACHE_KEY_PREFIX, key = "#product.id")
    public Mono<Product> update(Product product){
        return productRepository.existsById(product.getId())
                .flatMap(exists -> {
                    if(exists){
                        return productRepository.save(product);
                    }
                    else throw  new ResourceNotFoundException("Product with id " + product.getId() + " not found.");
                })
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException("Product with id " + product.getId() + " not found.")));

    }

    private Flux<Product> getFromRepository(String search, Pageable pageable){
        return (search == null || search.isEmpty())
                ? productRepository.findByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable);
    }

    private Mono<Void> clearAllProductsCache() {
        return reactiveRedisTemplate.keys(ALL_PRODUCTS_CACHE_KEY_PREFIX + "*")  // Получаем все ключи, начинающиеся с префикса
                .flatMap(reactiveRedisTemplate::delete) // Удаляем каждый ключ
                .then(); // Возвращаем Mono<Void>
    }

}
