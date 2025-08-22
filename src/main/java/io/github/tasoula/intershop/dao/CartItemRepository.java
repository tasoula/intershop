package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem>  findByUserIdAndProductId(UUID userId, UUID id);

    List<CartItem> findByUserId(UUID userId);
    List<CartItem> findByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    @Query("SELECT SUM(p.price * ci.quantity) " +
            "FROM CartItem ci JOIN ci.product p " +
            "WHERE ci.user.id = :userId")
    BigDecimal calculateTotalPriceByUserId(@Param("userId") UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
