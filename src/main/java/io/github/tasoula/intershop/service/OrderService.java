package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

/*     private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

   private final EntityManager entityManager;

    public OrderService(CartItemRepository cartItemRepository, OrderRepository orderRepository, EntityManager entityManager) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Optional<UUID> createOrder(UUID userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        // Если корзина пуста, не создаем заказ
        if (cartItems.isEmpty()) {
            return Optional.empty();
        }
        // Проверяем, достаточно ли товаров на складе.
        if (cartItems.stream().anyMatch(c -> c.getProduct().getStockQuantity() < c.getQuantity()))
            return Optional.empty();

        Order order = new Order();
        order.setUser(entityManager.getReference(User.class, userId));

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    Product product = cartItem.getProduct();
                    orderItem.setProduct(product);
                    orderItem.setPriceAtTimeOfOrder(product.getPrice());
                    orderItem.setQuantity(cartItem.getQuantity());
                    product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                    return orderItem;
                }).toList();
        order.setOrderItems(orderItems);

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.valueOf(0));
        order.setTotalAmount(totalAmount);

        UUID orderId = orderRepository.save(order).getId();
        cartItemRepository.deleteByUserId(userId);

        return Optional.of(orderId);
    }

    public Optional<OrderDto> getById(UUID id) {
        return orderRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<OrderDto> getByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    private OrderDto convertToDto(Order order) {
        List<ProductDto> productDtos = order.getOrderItems()
                .stream()
                .map(item -> {
                    ProductDto dto = new ProductDto();
                    dto.setId(item.getProduct().getId());
                    dto.setTitle(item.getProduct().getTitle());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPriceAtTimeOfOrder());
                    return dto;
                })
                .toList();

        BigDecimal totalAmount = productDtos.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderDto(
                order.getId(),
                productDtos,
                totalAmount
        );
    }

 */
}


