package io.github.tasoula.intershop.service;

import io.github.tasoula.client.domain.Amount;
import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.PaymentException;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Order;
import io.github.tasoula.intershop.model.OrderItem;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductDataService productDataService;
    private final WebClient webClient;
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        ProductDataService productDataService,
                        WebClient balanceWebClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productDataService = productDataService;
        this.webClient = balanceWebClient;
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
                .flatMap(cartItem -> productDataService.findById(cartItem.getProductId())
                        .map(product -> product.getStockQuantity() >= cartItem.getQuantity())
                )
                .all(Boolean::booleanValue); // Проверяем, что все продукты есть в нужном количестве
    }

    public Mono<Void> processPayment(UUID userId, BigDecimal totalAmount) {
        Amount amountRequest = new Amount();
        amountRequest.setAmount(totalAmount);

        return webClient.post()
                .uri("/payment/" + userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(amountRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        // Successful payment
                        return Mono.empty(); // Signal completion without data
                    } else if (response.statusCode().equals(HttpStatus.PAYMENT_REQUIRED)){
                        throw new PaymentException("Оплата не прошла (недостаточно средств)");
                    } else if(response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        throw new NoSuchElementException("Оплата не прошла (счет не найден)");
                    } else {
                        String errorMessage = "Оплата не прошла";
                        switch (response.statusCode()){
                            case HttpStatus.BAD_REQUEST -> errorMessage += " (неверный запрос)";
                            case HttpStatus.INTERNAL_SERVER_ERROR -> errorMessage += " (внутрення ошибка сервера платежей)";
                            case null, default -> errorMessage += " (Unexpected status code)";
                        }
                        String finalErrorMessage = errorMessage;
                        throw new RuntimeException(finalErrorMessage);
                    }
                });
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
                                BigDecimal totalAmount = countTotalAmount(orderItems);
                                savedOrder.setTotalAmount(totalAmount);
                                // Сохраняем обновленный заказ, очищаем корзину и возвращаем ID заказа
                                return orderRepository.save(savedOrder)
                                        .then(cartItemRepository.deleteByUserId(userId))
                                        .then(processPayment(userId, totalAmount))
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
                .flatMap(cartItem -> productDataService.findById(cartItem.getProductId())
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
                            return productDataService.save(product)
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
        return productDataService.findById(orderItem.getProductId())
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


