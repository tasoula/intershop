package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Flux<ProductDto> findByUserId(UUID userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .map(product -> new ProductDto(product, cartItem.getQuantity())));
    }


    public Mono<Integer> getCartQuantity(UUID userId, UUID productId) {
        if (userId == null) {
            return Mono.just(0);
        }
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .map(CartItem::getQuantity)
                .defaultIfEmpty(0); // Если нет записи в корзине, то 0
    }

    @Transactional
    public Mono<Integer> changeProductQuantityInCart(UUID userId, UUID productId, int changeQuantity) {
        return  productRepository.findById(productId)
                .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException("Product with id " + productId + " not found.")))
                .flatMap(product -> cartItemRepository.findByUserIdAndProductId(userId, productId)
                        .switchIfEmpty(Mono.just(new CartItem(userId, productId))) // Создаем новый CartItem с quantity = 0
                        .flatMap(cartItem -> {
                            int newQuantity = cartItem.getQuantity() + changeQuantity;

                            if (newQuantity <= 0) {
                                return cartItemRepository.deleteByUserIdAndProductId(userId, productId)
                                        .then(Mono.just(0)); // Возвращаем 0, так как элемент удален
                            }

                            newQuantity = Math.min(newQuantity, product.getStockQuantity());
                            cartItem.setQuantity(newQuantity);
                            return cartItemRepository.save(cartItem)
                                    .map(CartItem::getQuantity); // Возвращаем новую quantity
                        }));
    }

    @Transactional
    public Mono<Void> deleteCartItem(UUID userId, UUID productId) {
        return cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public Mono<BigDecimal> calculateTotalPriceByUserId(UUID userId) {
        return cartItemRepository.calculateTotalPriceByUserId(userId)
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));
    }

    public Mono<Boolean> isEmpty(UUID userId) {
        return cartItemRepository.existsByUserId(userId).map(exists -> !exists);
    }

}
