package io.github.tasoula.intershop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.dao.OrderItemRepository;
import io.github.tasoula.intershop.dao.OrderRepository;
import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.OrderDto;
import io.github.tasoula.intershop.exceptions.PaymentException;
import io.github.tasoula.intershop.model.*;
import io.github.tasoula.intershop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductDataService productRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private OrderService orderService;

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
    void setUp() {
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
    }

    @Test
    void getById_OrderExists_ReturnsOrderDto() {
        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.fromIterable(List.of(orderItem1, orderItem2)));
        when(productRepository.findById(orderItem1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderItem2.getProductId())).thenReturn(Mono.just(product2));

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
        when(productRepository.findById(orderItem1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderItem2.getProductId())).thenReturn(Mono.just(product2));

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
    void createOrder_SuccessfulOrderCreation_ReturnsOrderId() {
        // Mocking repository calls
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(productId1)).thenReturn(Mono.just(product1));
        when(productRepository.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem1),Mono.just(orderItem2));
        when(productRepository.update(any(Product.class))).thenReturn(Mono.just(product1),Mono.just(product2)); // Simulate stock update success
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        //Mocking webClient
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<Void>> exchangeToMono = invocation.getArgument(0);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);
            return exchangeToMono.apply(clientResponse);
        });

        // Execute the createOrder method
        Mono<UUID> result = orderService.createOrder(userId);

        // Verify the result using StepVerifier
        StepVerifier.create(result)
                .expectNext(orderId)
                .verifyComplete();

        // Verify that the repository methods were called as expected
        verify(cartItemRepository).findByUserId(userId);
        verify(productRepository, times(2)).findById(productId1);
        verify(productRepository, times(2)).findById(productId2);

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(productRepository, times(2)).update(any(Product.class));
        verify(cartItemRepository).deleteByUserId(userId);
    }

    @Test
    void createOrder_paymentRequired_throwsPaymentException() {
        // Mocking repository calls
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(productId1)).thenReturn(Mono.just(product1));
        when(productRepository.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem1),Mono.just(orderItem2));
        when(productRepository.update(any(Product.class))).thenReturn(Mono.just(product1),Mono.just(product2)); // Simulate stock update success
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        //Mocking webClient
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        ClientResponse clientResponse = mock(ClientResponse.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<Void>> exchangeToMono = invocation.getArgument(0);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.PAYMENT_REQUIRED);
            return exchangeToMono.apply(clientResponse);
        });

        // Execute the createOrder method
        Mono<UUID> result = orderService.createOrder(userId);

        // Verify the result using StepVerifier
        StepVerifier.create(result)
                .expectError(PaymentException.class)
                .verify();

        // Verify that the repository methods were called as expected
        verify(cartItemRepository).findByUserId(userId);
        verify(productRepository, times(2)).findById(productId1);
        verify(productRepository, times(2)).findById(productId2);

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(productRepository, times(2)).update(any(Product.class));
        verify(cartItemRepository).deleteByUserId(userId);
    }

    @Test
    void createOrder_notFound_throwsNoSuchElementException() {
        // Mocking repository calls
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(productId1)).thenReturn(Mono.just(product1));
        when(productRepository.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem1),Mono.just(orderItem2));
        when(productRepository.update(any(Product.class))).thenReturn(Mono.just(product1),Mono.just(product2)); // Simulate stock update success
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        //Mocking webClient
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        ClientResponse clientResponse = mock(ClientResponse.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<Void>> exchangeToMono = invocation.getArgument(0);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.NOT_FOUND);
            return exchangeToMono.apply(clientResponse);
        });

        // Execute the createOrder method
        Mono<UUID> result = orderService.createOrder(userId);

        // Verify the result using StepVerifier
        StepVerifier.create(result)
                .expectError(NoSuchElementException.class)
                .verify();

        // Verify that the repository methods were called as expected
        verify(cartItemRepository).findByUserId(userId);
        verify(productRepository, times(2)).findById(productId1);
        verify(productRepository, times(2)).findById(productId2);

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(productRepository, times(2)).update(any(Product.class));
        verify(cartItemRepository).deleteByUserId(userId);
    }


    @Test
    void createOrder_badRequest_throwsRuntimeException() {
        // Mocking repository calls
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(productId1)).thenReturn(Mono.just(product1));
        when(productRepository.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem1),Mono.just(orderItem2));
        when(productRepository.update(any(Product.class))).thenReturn(Mono.just(product1),Mono.just(product2)); // Simulate stock update success
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        //Mocking webClient
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        ClientResponse clientResponse = mock(ClientResponse.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<Void>> exchangeToMono = invocation.getArgument(0);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            return exchangeToMono.apply(clientResponse);
        });

        // Execute the createOrder method
        Mono<UUID> result = orderService.createOrder(userId);

        // Verify the result using StepVerifier
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().contains("неверный запрос"))
                .verify();

        // Verify that the repository methods were called as expected
        verify(cartItemRepository).findByUserId(userId);
        verify(productRepository, times(2)).findById(productId1);
        verify(productRepository, times(2)).findById(productId2);

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(productRepository, times(2)).update(any(Product.class));
        verify(cartItemRepository).deleteByUserId(userId);
    }

    @Test
    void createOrder_internalServerError_throwsRuntimeException() {
        // Mocking repository calls
        when(cartItemRepository.findByUserId(userId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(productId1)).thenReturn(Mono.just(product1));
        when(productRepository.findById(productId2)).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem1),Mono.just(orderItem2));
        when(productRepository.update(any(Product.class))).thenReturn(Mono.just(product1),Mono.just(product2)); // Simulate stock update success
        when(cartItemRepository.deleteByUserId(userId)).thenReturn(Mono.empty());

        //Mocking webClient
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        ClientResponse clientResponse = mock(ClientResponse.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any(Function.class))).thenAnswer(invocation -> {
            Function<ClientResponse, Mono<Void>> exchangeToMono = invocation.getArgument(0);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchangeToMono.apply(clientResponse);
        });

        // Execute the createOrder method
        Mono<UUID> result = orderService.createOrder(userId);

        // Verify the result using StepVerifier
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().contains("внутрення ошибка сервера платежей"))
                .verify();

        // Verify that the repository methods were called as expected
        verify(cartItemRepository).findByUserId(userId);
        verify(productRepository, times(2)).findById(productId1);
        verify(productRepository, times(2)).findById(productId2);

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(productRepository, times(2)).update(any(Product.class));
        verify(cartItemRepository).deleteByUserId(userId);
    }

}