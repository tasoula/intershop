package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
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
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;


    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<ProductDto> findByUserId(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return items.stream()
                .map(item -> new ProductDto(item.getProduct(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public int changeProductQuantityInCart(UUID userId, UUID productId, int changeQuantity) {
        //todo обработка productId = null
        Product product = productRepository.findById(productId).get();

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> new CartItem(new User(userId), product));

        int newQuantity = cartItem.getQuantity() + changeQuantity;

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            return 0;
        }

        if (newQuantity > product.getStockQuantity()) newQuantity = product.getStockQuantity();

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
        return (result != null) ? result : BigDecimal.valueOf(0);
    }

    public boolean isEmpty(UUID userId) {
        return !cartItemRepository.existsByUserId(userId);
    }

}
