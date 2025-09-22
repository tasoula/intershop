package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class ProductService {

    private final ProductDataService productDataService;
    private final CartService cartService;

    public ProductService(ProductDataService productDataService, CartService cartService) {
        this.productDataService = productDataService;
        this.cartService = cartService;
    }

    public Mono<Page<ProductDto>> findAll(UUID userId, String search, Pageable pageable) {
        return productDataService.findAll(search, pageable)
                .flatMap(product -> mapToDto(userId, product))
                .collectList()
                .zipWith(productDataService.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private Mono<ProductDto> mapToDto(UUID userId, Product product) {
        return  cartService.getCartQuantity(userId, product.getId())
                .map(cartQuantity -> new ProductDto(product, cartQuantity));
    }

    @Cacheable(value = "products", key = "#userId + '-' + #productId")
    public Mono<ProductDto> findById(UUID userId, UUID productId) {
        return productDataService.findById(productId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product not found with id: " + productId)))
                .flatMap(product -> mapToDto(userId, product));
    }

    public Mono<Void> createProduct(ProductDto productDto, FilePart image) {
        Product product = new Product();
        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        String filename = UUID.randomUUID() + "_" + image.filename();
        product.setImgPath(filename);
        return productDataService.createProduct(product, image);
    }
}
