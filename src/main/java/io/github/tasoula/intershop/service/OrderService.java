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

  //  @Transactional
    public Mono<UUID> createOrder(UUID userId) {
        // 1. Получаем Flux<CartItem> (асинхронный поток элементов корзины) для данного userId из репозитория.
        Flux<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        // 2. Собираем все элементы из Flux<CartItem> в List<CartItem>.  `collectList()` возвращает `Mono<List<CartItem>>`.
        return cartItems.collectList()
                // 3. `flatMap` позволяет работать с результатом `Mono<List<CartItem>>` асинхронно.
                .flatMap(items -> {
                    // 4. Если корзина пуста (`items.isEmpty()`), возвращаем `Mono.empty()`, что означает отсутствие результата (заказ не создан).
                    if (items.isEmpty()) {
                        return Mono.empty(); // Возвращаем Mono.empty() если корзина пуста
                    }

                    // 5. Создаем поток `Flux<Boolean>` для проверки наличия достаточного количества каждого товара на складе.
                    Flux<Boolean> stockCheckResults = Flux.fromIterable(items)
                            // 6. Для каждого элемента корзины (`cartItem`) находим соответствующий продукт в репозитории.
                            .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                                    // 7. Проверяем, достаточно ли товара на складе (stockQuantity >= quantity в корзине).
                                    .map(product -> product.getStockQuantity() >= cartItem.getQuantity()));

                    // 8. Проверяем, что *все* элементы в `stockCheckResults` равны `true` (то есть всех товаров достаточно).
                    return stockCheckResults.all(result -> result)
                            // 9. `flatMap` для работы с результатом `Mono<Boolean>` (все ли товары в наличии).
                            .flatMap(allInStock -> {
                                // 10. Если не все товары в наличии (`!allInStock`), возвращаем `Mono.empty()` (заказ не создан).
                                if (!allInStock) {
                                    return Mono.empty(); // Возвращаем Mono.empty() если товаров недостаточно
                                }

                                // 11. Создаем новый объект `Order`.
                                Order order = new Order();
                                // 12. Устанавливаем userId для заказа.
                                order.setUserId(userId);
                                order.setCreatedAt(Timestamp.from(Instant.now()));
                                // 13. Сохраняем заказ в репозитории.  `orderRepository.save(order)` возвращает `Mono<Order>`.
                                return orderRepository.save(order)
                                        // 14. `flatMap` для работы с сохраненным заказом.
                                        .flatMap(savedOrder -> {
                                            // 15. Получаем ID сохраненного заказа.
                                            UUID orderId = savedOrder.getId();

                                            // 16. Создаем поток `Flux<OrderItem>` для создания элементов заказа (`OrderItem`).
                                            Flux<OrderItem> orderItemsFlux = Flux.fromIterable(items)
                                                    // 17. Для каждого элемента корзины находим соответствующий продукт.
                                                    .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                                                            // 18. `flatMap` для работы с найденным продуктом.
                                                            .flatMap(product -> {
                                                                // 19. Создаем новый `OrderItem`.
                                                                OrderItem orderItem = new OrderItem();
                                                                // 20. Устанавливаем orderId для элемента заказа.
                                                                orderItem.setOrderId(orderId);
                                                                // 21. Устанавливаем productId.
                                                                orderItem.setProductId(cartItem.getProductId());
                                                                // 22. Устанавливаем цену товара на момент заказа.
                                                                orderItem.setPriceAtTimeOfOrder(product.getPrice());
                                                                // 23. Устанавливаем количество.
                                                                orderItem.setQuantity(cartItem.getQuantity());
                                                                // 24. Уменьшаем количество товара на складе.
                                                                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                                                                // 25. Сохраняем измененный продукт в репозитории. `thenReturn(orderItem)` возвращает `OrderItem` после сохранения продукта.
                                                                return productRepository.save(product)
                                                                        .then(orderItemRepository.save(orderItem))
                                                                        .thenReturn(orderItem); // Сохраняем измененный продукт
                                                            }));

                                            // 26. Собираем все `OrderItem` в `List<OrderItem>`.
                                            return orderItemsFlux.collectList()
                                                    // 27. `flatMap` для работы с `List<OrderItem>`.
                                                    .flatMap(orderItems -> {
                                                        // 29. Вычисляем общую стоимость заказа.
                                                        BigDecimal totalAmount = orderItems.stream()
                                                                .map(item -> item.getPriceAtTimeOfOrder().multiply(BigDecimal.valueOf(item.getQuantity())))
                                                                .reduce(BigDecimal::add)
                                                                .orElse(BigDecimal.valueOf(0));

                                                        // 30. Устанавливаем общую стоимость заказа.
                                                        savedOrder.setTotalAmount(totalAmount);

                                                        // 31. Сохраняем обновленный заказ.
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


