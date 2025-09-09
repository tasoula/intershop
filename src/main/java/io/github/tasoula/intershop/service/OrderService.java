package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.*;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private final TransactionalOperator transactionalOperator;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, TransactionalOperator transactionalOperator) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Transactional
    public Mono<UUID> createOrder(UUID userId) {
        Flux<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return cartItems.collectList()
                .flatMap(items -> {
                    if (items.isEmpty()) {
                        return Mono.empty(); // Возвращаем Mono.empty() если корзина пуста
                    }

                    Flux<Boolean> stockCheckResults = Flux.fromIterable(items)
                            .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                                    .map(product -> product.getStockQuantity() >= cartItem.getQuantity()));

                    return stockCheckResults.all(result -> result)
                            .flatMap(allInStock -> {
                                if (!allInStock) {
                                    return Mono.empty();
                                }

                                Order order = new Order();
                                order.setUserId(userId);
                                order.setCreatedAt(Timestamp.from(Instant.now()));
                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> {
                                            UUID orderId = savedOrder.getId();

                                            Flux<OrderItem> orderItemsFlux = Flux.fromIterable(items)
                                                    .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                                                            .flatMap(product -> {
                                                                OrderItem orderItem = new OrderItem();
                                                                orderItem.setOrderId(orderId);
                                                                orderItem.setProductId(cartItem.getProductId());
                                                                orderItem.setPriceAtTimeOfOrder(product.getPrice());
                                                                orderItem.setQuantity(cartItem.getQuantity());
                                                                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                                                                return productRepository.save(product)
                                                                        .then(orderItemRepository.save(orderItem))
                                                                        .thenReturn(orderItem); // Сохраняем измененный продукт
                                                            }));

                                            return orderItemsFlux.collectList()
                                                    .flatMap(orderItems -> {
                                                        BigDecimal totalAmount = orderItems.stream()
                                                                .map(item -> item.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                                                                .reduce(BigDecimal::add)
                                                                .orElse(BigDecimal.valueOf(0));

                                                        savedOrder.setTotalAmount(totalAmount);

                                                        return orderRepository.save(savedOrder)
                                                                // 32. Удаляем товары из корзины пользователя.
                                                                .then(cartItemRepository.deleteByUserId(userId))
                                                                // 33. Возвращаем orderId.
                                                                .thenReturn(orderId); // Возвращаем orderId
                                                    });
                                        });
                            });
                });
    }




    public Mono<OrderDto> getById(UUID id) {
        return orderRepository.findById(id)
                .flatMap(this::convertToDto);
    }


    public Flux<OrderDto> getByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(this::convertToDto);
    }


    private Mono<OrderDto> convertToDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(orderItem ->
                        productRepository.findById(orderItem.getProductId())
                                .map(product -> { // Используем map для прямого преобразования Product -> ProductDto
                                    ProductDto dto = new ProductDto();
                                    dto.setId(product.getId());
                                    dto.setTitle(product.getTitle());
                                    dto.setQuantity(orderItem.getQuantity());
                                    dto.setPrice(orderItem.getPriceAtTimeOfOrder());
                                    return dto; // Возвращаем заполненный dto
                                })
                )
                .collectList()
                .map(productDtos -> {
                    BigDecimal totalAmount = productDtos.stream()
                            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new OrderDto(
                            order.getId(),
                            productDtos,
                            totalAmount
                    );
                });
    }
}


