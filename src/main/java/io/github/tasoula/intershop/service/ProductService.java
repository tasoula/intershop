package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartService cartService;

    private final ImageService imageService;


    public ProductService(ProductRepository repository, CartService cartService, ImageService imageService) {
        this.productRepository = repository;
        this.cartService = cartService;
        this.imageService = imageService;
    }

    public Page<ProductDto> findAll(UUID userId, String search, Pageable pageable) {
       Page<Product> productPage = (search == null || search.isEmpty())
                ? productRepository.findAllByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(search.toLowerCase(), search.toLowerCase(), 0, pageable);

        List<ProductDto> productCatalogItemDtos = productPage.getContent().stream()
                .map(product -> mapToDto(userId, product))
                .collect(Collectors.toList());

        return new PageImpl<>(productCatalogItemDtos, pageable, productPage.getTotalElements());
    }

    public ProductDto findById(UUID userId, UUID productId) throws ResourceNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDto(userId, product);
    }

    private ProductDto mapToDto(UUID userId, Product product) {
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


}
