package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductCatalogItemDto;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository repository, CartItemRepository cartItemRepository) {
        this.productRepository = repository;
        this.cartItemRepository = cartItemRepository;
    }

    public Page<ProductCatalogItemDto> findAll(UUID userId, String search, Pageable pageable) {
        Page<Product> productPage = (search == null || search.isEmpty()) ? productRepository.findAll(pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCase(search.toLowerCase(), pageable);


        List<ProductCatalogItemDto> productCatalogItemDtos = productPage.getContent().stream()
                .map(product -> map(userId, product))
                .collect(Collectors.toList());

        return new PageImpl<>(productCatalogItemDtos, pageable, productPage.getTotalElements());
    }

    public ProductCatalogItemDto findById(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId).get();
        //todo: обработка product == null

        return map(userId, product);
    }

    private ProductCatalogItemDto map(UUID userId, Product product){
        int cartQuantity = 0;
        if(userId != null) {
            cartQuantity = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                    .map(CartItem::getQuantity)
                    .orElse(0); // Если нет записи в корзине, то 0
        }

        return new ProductCatalogItemDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getImgPath(),
                product.getPrice(),
                product.getStockQuantity(),
                cartQuantity
        );
    }


    @Transactional
    public int changeCartQuantity(UUID userId, UUID productId, int changeQuantity) {
        //todo обработка null
        Product product = productRepository.findById(productId).get();

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> new CartItem(null, new User(userId, null),  product, 0));

        int newQuantity = cartItem.getQuantity() + changeQuantity;
        if(newQuantity >=0 && newQuantity <= product.getStockQuantity()) {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

        return cartItem.getQuantity();
    }
}
