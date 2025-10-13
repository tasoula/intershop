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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
    private final ReactiveOAuth2AuthorizedClientManager manager;
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        ProductDataService productDataService,
                        WebClient balanceWebClient,
                        ReactiveOAuth2AuthorizedClientManager manager) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productDataService = productDataService;
        this.webClient = balanceWebClient;
        this.manager = manager;
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

    private Mono<Void> processPayment(UUID userId, BigDecimal totalAmount) {
        Amount amountRequest = new Amount();
        amountRequest.setAmount(totalAmount);
        return manager.authorize(OAuth2AuthorizeRequest
                                .withClientRegistrationId("store")
                                .principal("system") //??? У client_credentials нет имени пользователя, поэтому будем использовать system
                                .build()) // Mono<OAuth2AuthorizedClient>
                        .map(OAuth2AuthorizedClient::getAccessToken)
                        .map(OAuth2AccessToken::getTokenValue)
                        .flatMap(accessToken -> webClient.post()
                                            .uri("/payment/" + userId.toString())
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(amountRequest)
                                            .retrieve()
                                            .onStatus(HttpStatus.PAYMENT_REQUIRED::equals,
                                                    response -> Mono.error(new PaymentException("Оплата не прошла (недостаточно средств)")))
                                            .onStatus(HttpStatus.NOT_FOUND::equals,
                                                    response -> Mono.error(new NoSuchElementException("Оплата не прошла (счет не найден)")))
                                            .onStatus(HttpStatus.BAD_REQUEST::equals,
                                                    response -> Mono.error(new RuntimeException("Оплата не прошла (неверный запрос)")))
                                            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                                                    response -> Mono.error(new RuntimeException("Оплата не прошла (внутрення ошибка сервера платежей)")))
                                            .onStatus(status -> !status.is2xxSuccessful(),  // Обработка всех остальных ошибок (не 2xx)
                                                    response -> {
                                                        String errorMessage = "Оплата не прошла (Unexpected status code): " + response.statusCode();
                                                        return Mono.error(new RuntimeException(errorMessage));
                                                    })
                                            .bodyToMono(Void.class)// Обрабатывает успешный статус (200 OK)

                        );
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
                            return productDataService.update(product)
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


