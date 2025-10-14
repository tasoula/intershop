package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class ProductDataService {
    private final ProductRepository productRepository;
    private final ImageService imageService;


    private final String ALL_PRODUCTS_CACHE_KEY_PREFIX = "products_all";

    private final String PRODUCTS_CACHE_KEY_PREFIX = "products";

    public ProductDataService(ProductRepository repository, ImageService imageService) {
        this.productRepository = repository;
        this.imageService = imageService;
    }

    @Cacheable(value = ALL_PRODUCTS_CACHE_KEY_PREFIX, key = "#search + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Mono<List<Product>> findAll(String search, Pageable pageable) {
        return ((search == null || search.isEmpty())
                ? productRepository.findByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                        search.toLowerCase(), search.toLowerCase(), 0, pageable))
                .collectList();
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
    @CacheEvict(value = ALL_PRODUCTS_CACHE_KEY_PREFIX, allEntries = true)
    public Mono<Void> createProduct(Product product, FilePart image) {
        return productRepository.save(product)
                .flatMap(savedProduct -> imageService.saveToDisc(image, product.getImgPath())
                        .thenReturn(savedProduct))
                .then(); // Очищаем кеш после создания продукта
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
}
