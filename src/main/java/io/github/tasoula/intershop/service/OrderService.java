package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Order;
import io.github.tasoula.intershop.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Mono<OrderDto> getById(UUID id) {
        return orderRepository.findById(id)
                .flatMap(this::convertToDto);
    }


    public Flux<OrderDto> getByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(this::convertToDto);
    }

    @Transactional
    public Mono<UUID> createOrder(UUID userId) {
        // Получаем все элементы корзины пользователя
        return cartItemRepository.findByUserId(userId)
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        // Если корзина пуста, возвращаем пустой Mono
                        return Mono.empty();
                    }
                    return checkStockAvailability(cartItems)
                            .flatMap(allInStock -> (allInStock) ? createAndSaveOrder(userId, cartItems) : Mono.empty());
                });
    }

    private Mono<Boolean> checkStockAvailability(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .map(product -> product.getStockQuantity() >= cartItem.getQuantity())
                )
                .all(Boolean::booleanValue); // Проверяем, что все продукты есть в нужном количестве
    }

    private Mono<UUID> createAndSaveOrder(UUID userId, List<CartItem> cartItems) {
        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(Timestamp.from(Instant.now()));

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    UUID orderId = savedOrder.getId();
                    // Создаем элементы заказа и обновляем количество товара на складе
                    return saveOrderItemsAndUpdateStock(cartItems, orderId)
                            .flatMap(orderItems -> {
                                savedOrder.setTotalAmount(countTotalAmount(orderItems));
                                // Сохраняем обновленный заказ, очищаем корзину и возвращаем ID заказа
                                return orderRepository.save(savedOrder)
                                        .then(cartItemRepository.deleteByUserId(userId))
                                        .thenReturn(orderId);
                            });
                });
    }

    private BigDecimal countTotalAmount(List<OrderItem> orderItems){
        return orderItems.stream()
                .map(item -> item.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<List<OrderItem>> saveOrderItemsAndUpdateStock(List<CartItem> cartItems, UUID orderId) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .flatMap(product -> {
                            // Создаем элемент заказа
                            OrderItem orderItem = new OrderItem();
                            orderItem.setOrderId(orderId);
                            orderItem.setProductId(cartItem.getProductId());
                            orderItem.setPriceAtTimeOfOrder(product.getPrice());
                            orderItem.setQuantity(cartItem.getQuantity());

                            // Обновляем количество товара на складе
                            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

                            // Сохраняем обновленный продукт и элемент заказа
                            return productRepository.save(product)
                                    .then(orderItemRepository.save(orderItem))
                                    .thenReturn(orderItem);
                        })
                )
                .collectList();
    }

    public Mono<OrderDto> convertToDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(this::convertToProductDto)  // Используем отдельный метод для преобразования
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

    private Mono<ProductDto> convertToProductDto(OrderItem orderItem) {
        return productRepository.findById(orderItem.getProductId())
                .map(product -> {
                    ProductDto dto = new ProductDto();
                    dto.setId(product.getId());
                    dto.setTitle(product.getTitle());
                    dto.setQuantity(orderItem.getQuantity());
                    dto.setPrice(orderItem.getPriceAtTimeOfOrder());
                    return dto;
                });
    }
}


