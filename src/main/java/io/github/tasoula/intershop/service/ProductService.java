package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;


    public ProductService(ProductRepository repository, CartItemRepository cartItemRepository) {
        this.productRepository = repository;

        this.cartItemRepository = cartItemRepository;
    }

    public Page<ProductDto> findAll(UUID userId, String search, Pageable pageable) {
       Page<Product> productPage = (search == null || search.isEmpty())
                ? productRepository.findAllByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(search.toLowerCase(), search.toLowerCase(), 0, pageable);

        List<ProductDto> productCatalogItemDtos = productPage.getContent().stream()
                .map(product -> map(userId, product))
                .collect(Collectors.toList());

        return new PageImpl<>(productCatalogItemDtos, pageable, productPage.getTotalElements());
    }

    public ProductDto findById(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId).get();
        //todo: обработка product == null

        return map(userId, product);
    }

    public ProductDto map(UUID userId, Product product){
        int cartQuantity = 0;
        if(userId != null) {
            cartQuantity = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                    .map(CartItem::getQuantity)
                    .orElse(0); // Если нет записи в корзине, то 0
        }
        return new ProductDto(product, cartQuantity);
    }
}
