package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.exceptions.PaymentException;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Order;
import io.github.tasoula.intershop.model.OrderItem;
import io.github.tasoula.intershop.model.Product;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductDataService productDataService;
    @Mock
    private ReactiveOAuth2AuthorizedClientManager manager;
    @InjectMocks
    private OrderService orderService;
    private MockWebServer mockWebServer;
    private WebClient webClient;

    private UUID userId;
    private UUID orderId;
    private UUID productId1;
    private UUID productId2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private Product product1;
    private Product product2;
    private Order order;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() throws IOException {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();

        cartItem1 = new CartItem();
        cartItem1.setUserId(userId);
        cartItem1.setProductId(productId1);
        cartItem1.setQuantity(2);

        cartItem2 = new CartItem();
        cartItem2.setUserId(userId);
        cartItem2.setProductId(productId2);
        cartItem2.setQuantity(3);

        product1 = new Product();
        product1.setId(productId1);
        product1.setPrice(BigDecimal.TEN);
        product1.setStockQuantity(10);

        product2 = new Product();
        product2.setId(productId2);
        product2.setPrice(BigDecimal.valueOf(5));
        product2.setStockQuantity(15);

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setCreatedAt(Timestamp.from(Instant.now()));

        orderItem1 = new OrderItem();
        orderItem1.setOrderId(orderId);
        orderItem1.setProductId(productId1);
        orderItem1.setPriceAtTimeOfOrder(BigDecimal.TEN);
        orderItem1.setQuantity(2);

        orderItem2 = new OrderItem();
        orderItem2.setOrderId(orderId);
        orderItem2.setProductId(productId2);
        orderItem2.setPriceAtTimeOfOrder(BigDecimal.valueOf(5));
        orderItem2.setQuantity(3);

        orderDto = new OrderDto();
        orderDto.setId(orderId);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        webClient = WebClient.builder().baseUrl(baseUrl).build();
        orderService = new OrderService(orderRepository,
                orderItemRepository,
                cartItemRepository,
                productDataService,
                webClient,
                manager); // Re-inject dependencies
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Останавливаем MockWebServer после каждого теста
        mockWebServer.shutdown();
    }

    @Test
    void getById_OrderExists_ReturnsOrderDto() {
        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.fromIterable(List.of(orderItem1, orderItem2)));
        when(productDataService.findById(orderItem1.getProductId())).thenReturn(Mono.just(product1));
        when(productDataService.findById(orderItem2.getProductId())).thenReturn(Mono.just(product2));

        Mono<OrderDto> result = orderService.getById(orderId);

        OrderDto o = result.block();

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals(orderId))
                .verifyComplete();

        verify(orderRepository).findById(orderId);
    }

    @Test
    void getById_OrderDoesNotExist_ReturnsEmptyMono() {
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        Mono<OrderDto> result = orderService.getById(orderId);

        StepVerifier.create(result)
                .expectComplete();

        verify(orderRepository).findById(orderId);
    }

    @Test
    void getByUserId_OrdersExist_ReturnsFluxOfOrderDtos() {
        when(orderRepository.findByUserId(userId)).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.fromIterable(List.of(orderItem1, orderItem2)));
        when(productDataService.findById(orderItem1.getProductId())).thenReturn(Mono.just(product1));
        when(productDataService.findById(orderItem2.getProductId())).thenReturn(Mono.just(product2));

        Flux<OrderDto> result = orderService.getByUserId(userId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals(orderId))
                .verifyComplete();

        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void getByUserId_NoOrdersExist_ReturnsEmptyFlux() {
        when(orderRepository.findByUserId(userId)).thenReturn(Flux.empty());

        Flux<OrderDto> result = orderService.getByUserId(userId);

        StepVerifier.create(result)
                .expectComplete();

        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void createOrder_CartIsEmpty_ReturnsEmptyMono() {
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.empty());

        Mono<UUID> result = orderService.createOrder(userId);

        StepVerifier.create(result)
                .expectComplete();

        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void createOrder_NotInStock_ReturnsEmptyMono() {
        UUID productOutOfStockId = UUID.randomUUID();
        Product productOutOfStock = new Product();
        productOutOfStock.setId(productOutOfStockId);
        productOutOfStock.setPrice(BigDecimal.TEN);
        productOutOfStock.setStockQuantity(10);

        CartItem cartItemOutOfStock = new CartItem();
        cartItemOutOfStock.setUserId(userId);
        cartItemOutOfStock.setProductId(productOutOfStock.getId());
        cartItemOutOfStock.setQuantity(3);

        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItemOutOfStock));

        Mono<UUID> result = orderService.createOrder(userId);

        StepVerifier.create(result)
                .expectComplete();

        verify(cartItemRepository).findByUserId(userId);
    }

    @Test
    void createOrder_notEnoughStock() {
        // Arrange
        UUID productId = UUID.randomUUID();

        CartItem cartItem = new CartItem();
        cartItem.setProductId(productId);
        cartItem.setUserId(userId);
        cartItem.setQuantity(10);
        List<CartItem> cartItems = List.of(cartItem);

        Product product = new Product();
        product.setId(productId);
        product.setStockQuantity(5); // Not enough stock

        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.fromIterable(cartItems));
        when(productDataService.findById(productId)).thenReturn(Mono.just(product));

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectComplete() // Should complete without emitting anything because of empty Mono
                .verify();
    }

    @Test
    void createOrder_emptyCart() {
        // Arrange
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.empty());

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    private void prepareMockDependencies(){
        String mockAccessTokenValue = "mock-jwt-token";

        // 1. Mock Keycloak/OAuth2 Authorization Manager
        // Create a mock OAuth2AccessToken
        OAuth2AccessToken mockAccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                mockAccessTokenValue,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singleton("scope")
        );

        // Create a mock OAuth2AuthorizedClient
        OAuth2AuthorizedClient mockAuthorizedClient = new OAuth2AuthorizedClient(
                Mockito.mock(ClientRegistration.class), // Mock ClientRegistration
                "test-principal", // Principal name
                mockAccessToken,
                Mockito.mock(OAuth2RefreshToken.class) // Mock Refresh Token
        );

        // Configure manager.authorize() to return our mock client
        when(manager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(Mono.just(mockAuthorizedClient));

        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.fromIterable(List.of(cartItem1, cartItem2)));
        when(productDataService.findById(productId1)).thenReturn(Mono.just(product1));
        when(productDataService.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(productDataService.update(any(Product.class))).thenReturn(Mono.empty());
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(new OrderItem()));
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());
    }

    @Test
    void createOrder_success() {
        prepareMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200));

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(orderId)
                .verifyComplete();
    }

    @Test
    void createOrder_paymentFailed() {
        prepareMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.PAYMENT_REQUIRED.value())); // Payment Required

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectError(PaymentException.class)
                .verify();
    }

    @Test
    void createOrder_accountNotFount() {
        prepareMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())); // Payment Required

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    void createOrder_badRequest() {
        prepareMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())); // Payment Required

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void createOrder_paymentServerError() {
        prepareMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())); // Payment Required

        // Act
        Mono<UUID> result = orderService.createOrder(userId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}