package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem>  findByUserIdAndProductId(UUID userId, UUID id);

    List<CartItem> findByUserId(UUID userId);
}
