package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    /*   private final CartService cartService;

    private final ImageService imageService;


     */

    public ProductService(ProductRepository repository) {
        this.productRepository = repository;
    }

    public Mono<Page<ProductDto>> findAll(UUID userId, String search, Pageable pageable) {
        Flux<Product> productFlux = (search == null || search.isEmpty())
                ? productRepository.findByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                search.toLowerCase(), search.toLowerCase(), 0, pageable);

        return productFlux
                .flatMap(product -> mapToDto(userId, product))
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private Mono<ProductDto> mapToDto(UUID userId, Product product) {
        // Асинхронно получаем количество товара в корзине.  cartService.getCartQuantity возвращает Mono<Integer>
        return Mono.just(0) // todo cartService.getCartQuantity(userId, product.getId())
                .map(cartQuantity -> new ProductDto(product, cartQuantity)); // Преобразуем в ProductDto
    }


    /*  public ProductDto findById1(UUID userId, UUID productId) throws ResourceNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDto(userId, product);
    }

     */

    public Mono<ProductDto> findById(UUID userId, UUID productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product not found with id: " + productId)))
                .flatMap(product -> mapToDto(userId, product)); // Используем flatMap для преобразования Mono<Product> в Mono<ProductDto>
    }

 /*   private ProductDto mapToDto(UUID userId, Product product) {
        return new ProductDto(product, cartService.getCartQuantity(userId, product.getId()));
    }


    @Transactional
    public void createProduct(String title,
                              String description,
                              MultipartFile image,
                              BigDecimal price,
                              int stockQuantity) {

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);

        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        product.setImgPath(filename);

        productRepository.save(product);
        imageService.saveToDisc(image, filename);
    }

 */
}
