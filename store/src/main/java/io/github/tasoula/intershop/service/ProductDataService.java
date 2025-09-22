package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ProductDataService {
    private final ProductRepository productRepository;
    private final ImageService imageService;

    public ProductDataService(ProductRepository repository, ImageService imageService) {
        this.productRepository = repository;
        this.imageService = imageService;
    }

    public Flux<Product> findAll(String search, Pageable pageable) {
        return  (search == null || search.isEmpty())
                ? productRepository.findByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable);
    }

    public Mono<Long> count(){
        return productRepository.count();
    }

    @Cacheable(value = "products", key = "#userId + '-' + #productId")
    public Mono<Product> findById(UUID productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product not found with id: " + productId)));
    }

    @Transactional
    public Mono<Void> createProduct(Product product, FilePart image) {
        return save(product)
                .flatMap(savedProduct -> imageService.saveToDisc(image, product.getImgPath())
                        .thenReturn(savedProduct))
                .then();
    }

    public Mono<Product> save(Product product){
        return productRepository.save(product);
    }
}
