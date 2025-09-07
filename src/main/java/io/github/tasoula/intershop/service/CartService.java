package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    /*
    private final CartItemRepository cartItemRepository;
    private final EntityManager entityManager;


    public CartService(CartItemRepository cartItemRepository, EntityManager entityManager) {
        this.cartItemRepository = cartItemRepository;
        this.entityManager = entityManager;
    }

 /*   public List<ProductDto> findByUserId(UUID userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    private ProductDto convertToProductDto(CartItem item) {
        return new ProductDto(item.getProduct(), item.getQuantity());
    }

    @Transactional
    public int changeProductQuantityInCart(UUID userId, UUID productId, int changeQuantity) {

        Product product = entityManager.getReference(Product.class, productId);

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> new CartItem(entityManager.getReference(User.class, userId), product));

        int newQuantity = cartItem.getQuantity() + changeQuantity;

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            return 0;
        }

        newQuantity = Math.min(newQuantity, product.getStockQuantity());

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return cartItem.getQuantity();
    }

    @Transactional
    public void deleteCartItem(UUID userId, UUID productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public BigDecimal calculateTotalPriceByUserId(UUID userId) {
        BigDecimal result = cartItemRepository.calculateTotalPriceByUserId(userId);
        return (result != null) ? result : BigDecimal.ZERO;
    }

    public boolean isEmpty(UUID userId) {
        return !cartItemRepository.existsByUserId(userId);
    }

    public int getCartQuantity(UUID userId, UUID productId) {
        if (userId == null) {
            return 0;
        }
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .map(CartItem::getQuantity)
                .orElse(0); // Если нет записи в корзине, то 0
    }

  */
}
