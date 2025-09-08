package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.CartItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, UUID> {
    Mono<CartItem> findByUserIdAndProductId(UUID userId, UUID id);
    Flux<CartItem> findByUserId(UUID userId);
    Flux<CartItem> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Mono<Void> deleteByUserIdAndProductId(UUID userId, UUID productId);

    @Query("""
            SELECT SUM(p.price * ci.quantity) 
            FROM t_cart_items ci JOIN t_products p ON ci.product_id = p.id 
            WHERE ci.user_id = :userId
            """)
    Mono<BigDecimal> calculateTotalPriceByUserId(@Param("userId") UUID userId);

    Mono<Boolean> existsByUserId(UUID userId);

    Mono<Void> deleteByUserId(UUID userId);
}
