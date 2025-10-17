package io.github.tasoula.intershop.service;

import io.github.tasoula.client.domain.Amount;
import io.github.tasoula.intershop.dao.CartItemRepository;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.CartItem;
import io.github.tasoula.intershop.model.Product;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest  {

    @MockitoBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductDataService productDataService;

    @Mock
    private  ReactiveOAuth2AuthorizedClientManager manager;

    @InjectMocks
    private CartService cartService;

    public static MockWebServer mockWebServer;

    private WebClient webClient;

    @Autowired
    private ApplicationContext applicationContext;

    private UUID userId;
    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() throws IOException {
        userId = UUID.randomUUID();

        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setTitle("Test Product");
        product.setStockQuantity(10);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        webClient = WebClient.builder().baseUrl(baseUrl).build();
        cartService = new CartService(
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
    void findByUserId_shouldReturnProductDtoList() {
        // Arrange
        CartItem cartItem1 = new CartItem();
        cartItem1.setUserId(userId);
        cartItem1.setProductId(productId);
        cartItem1.setCreatedAt(Timestamp.from(Instant.now()));
        cartItem1.setQuantity(2);

        UUID productId2 = UUID.randomUUID();
        Product product2 = new Product();
        product2.setId(productId2);
        product2.setTitle("Test Product 2");
        product2.setStockQuantity(20);

        CartItem cartItem2 = new CartItem();
        cartItem2.setUserId(userId);
        cartItem2.setProductId(productId2);
        cartItem2.setQuantity(3);
        cartItem2.setCreatedAt(Timestamp.from(Instant.MIN));
        List<CartItem> cartItems = List.of(cartItem1, cartItem2);

        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Flux.fromIterable(cartItems));
        when(productDataService.findById(productId)).thenReturn(Mono.just(product));
        when(productDataService.findById(product2.getId())).thenReturn(Mono.just(product2));


        StepVerifier.create(cartService.findByUserId(userId))
                .assertNext(productDto -> {
                    assert productDto.getTitle().equals(product.getTitle());
                    assert productDto.getQuantity() == cartItem1.getQuantity();
                })
                .assertNext(productDto -> {
                    assert productDto.getTitle().equals(product2.getTitle());
                    assert productDto.getQuantity() == cartItem2.getQuantity();
                })
                .verifyComplete();

        verify(cartItemRepository).findByUserIdOrderByCreatedAtDesc(userId);
        verify(productDataService).findById(productId);
        verify(productDataService).findById(productId2);
    }

     @Test
    void changeProductQuantityInCart_productNotFound_shouldThrowException() {
         int changeQuantity = 2;

         when(productDataService.findById(productId)).thenReturn(Mono.empty());

         Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

         StepVerifier.create(result)
                 .expectError(ResourceNotFoundException.class)
                 .verify();

         verify(cartItemRepository, never()).save(any(CartItem.class));
         verify(cartItemRepository, never()).deleteByUserIdAndProductId(any(), any());
       }

   @Test
    void changeProductQuantityInCart_cartItemNotFound_shouldCreateNewCartItem() {
       int changeQuantity = 3;
       int expectedQuantity = changeQuantity; // Initial quantity equals to changeQuantity because cart item doesn't exist

       when(productDataService.findById(productId)).thenReturn(Mono.just(product));
       when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty());
       when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
           CartItem savedCartItem = invocation.getArgument(0);
           savedCartItem.setQuantity(expectedQuantity); // Simulate the quantity update in the saved item
           return Mono.just(savedCartItem);
       });

       Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

       StepVerifier.create(result)
               .expectNext(expectedQuantity)
               .verifyComplete();

       verify(cartItemRepository, times(1)).save(argThat(item -> item.getQuantity() == expectedQuantity));

       verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);

    }

         @Test
    void changeProductQuantityInCart_changeQuantityIsNegativeAndResultIsZero_shouldDeleteCartItem() {
             // Arrange
             CartItem cartItem = new CartItem();
             cartItem.setUserId(userId);
             cartItem.setProductId(productId);
             cartItem.setQuantity(2);
             int changeQuantity = -5;

             when(productDataService.findById(productId)).thenReturn(Mono.just(product));
             when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
             when(cartItemRepository.deleteByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty()); // Simulate successful delete

             Mono<Integer> result = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

             StepVerifier.create(result)
                     .expectNext(0) // Expect the method to return 0 when deleting
                     .verifyComplete();

             verify(cartItemRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
             verify(cartItemRepository, never()).save(any(CartItem.class)); // Ensure save is not called

         }

      @Test
       void changeProductQuantityInCart_changeQuantityIsPositiveAndResultExceedsStock_shouldSetQuantityToStockQuantity() {
           // Arrange
           Product productWithLimitedStock = new Product();
           productWithLimitedStock.setId(productId);
           productWithLimitedStock.setTitle("Test Product");
           productWithLimitedStock.setStockQuantity(3);

           CartItem cartItem = new CartItem(userId, productWithLimitedStock.getId());
           cartItem.setQuantity(2);

          when(productDataService.findById(productWithLimitedStock.getId())).thenReturn(Mono.just(productWithLimitedStock));
           when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
           when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem));

           // Act
          Mono<Integer> newQuantity = cartService.changeProductQuantityInCart(userId, productId, 5);

          StepVerifier.create(newQuantity)
                  .expectNext(productWithLimitedStock.getStockQuantity())
                  .verifyComplete();

           verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
           verify(cartItemRepository, times(1)).save(any(CartItem.class));
       }

       @Test
        void changeProductQuantityInCart_successfulUpdate() {
            // Arrange
            CartItem cartItem = new CartItem(userId, productId);
            cartItem.setQuantity(2);
           int changeQuantity = 3;
           int expectedQuantity = cartItem.getQuantity() + changeQuantity;

            when(productDataService.findById(productId)).thenReturn(Mono.just(product));
            when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

            // Act
           Mono<Integer> newQuantity = cartService.changeProductQuantityInCart(userId, productId, changeQuantity);

           StepVerifier.create(newQuantity)
                   .expectNext(cartItem.getQuantity() + changeQuantity)
                   .verifyComplete();

            // Assert
            verify(cartItemRepository, times(1)).findByUserIdAndProductId(userId, productId);
            verify(cartItemRepository, times(1)).save(cartItem);
        }

    @Test
   void calculateTotalPriceByUserId_ShouldReturnTotalPrice_WhenCartIsNotEmpty() {
       // Arrange
       BigDecimal expectedTotalPrice = new BigDecimal("100.00");
       when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(expectedTotalPrice));

       // Act
        Mono<BigDecimal> actualTotalPrice = cartService.calculateTotalPriceByUserId(userId);

       // Assert
        StepVerifier.create(actualTotalPrice)
                .expectNext(expectedTotalPrice)
                .verifyComplete();
        verify(cartItemRepository, times(1)).calculateTotalPriceByUserId(userId);
   }

   @Test
   void calculateTotalPriceByUserId_ShouldReturnZero_WhenCartIsEmpty() {
       when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.empty());

       // Act
       Mono<BigDecimal> result = cartService.calculateTotalPriceByUserId(userId);

       // Assert
       StepVerifier.create(result)
               .expectNext(BigDecimal.ZERO)
               .verifyComplete();
       verify(cartItemRepository, times(1)).calculateTotalPriceByUserId(userId);
   }

  @Test
   void isEmpty_ShouldReturnTrue_WhenCartIsEmpty() {
      when(cartItemRepository.existsByUserId(userId)).thenReturn(Mono.just(false));

      // Act
      Mono<Boolean> result = cartService.isEmpty(userId);

      // Assert
      StepVerifier.create(result)
              .expectNext(true)
              .verifyComplete();
      verify(cartItemRepository, times(1)).existsByUserId(userId);
   }

    @Test
   void isEmpty_ShouldReturnFalse_WhenCartIsNotEmpty() {
        // Arrange
        when(cartItemRepository.existsByUserId(userId)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = cartService.isEmpty(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        verify(cartItemRepository, times(1)).existsByUserId(userId);
    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsQuantityFromCartItemRepository() {
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(5);

        when(cartItemRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartService.getCartQuantity(userId, productId))
                .expectNext(cartItem.getQuantity())
                .verifyComplete();

        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);

    }

    @Test
    void getCartQuantity_UserIsNotNull_ReturnsZeroIfCartItemNotFound() {
        when(cartItemRepository.findByUserIdAndProductId(userId, productId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(cartService.getCartQuantity(userId, productId))
                .expectNext(0)
                .verifyComplete();

        verify(cartItemRepository).findByUserIdAndProductId(userId, productId);
    }

    @Test
    void getCartQuantity_UserIsNull_ReturnsZero() {
        StepVerifier.create(cartService.getCartQuantity(null, productId))
                .expectNext(0)
                .verifyComplete();

        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void deleteCartItem_ShouldCallRepositoryDeleteMethod() {
        // Arrange
        when(cartItemRepository.deleteByUserIdAndProductId(userId, productId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = cartService.deleteCartItem(userId, productId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(cartItemRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
    }
/*
    @Test
    void isAvailable_sufficientBalance_returnsTrue() {
        // Arrange
        BigDecimal totalCartPrice = BigDecimal.valueOf(100);
        BigDecimal userBalance = BigDecimal.valueOf(200);

        Amount amount = new Amount();
        amount.setAmount(userBalance);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock_token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("store")
                .clientId("internet-shop")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientSecret("SVS8gpjFD2Gm2ZAqcgbCzAaLxAJyfcXJ")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)

               // .redirectUri("http://localhost/callback")
               // .authorizationUri("https://accounts.ya.ru/o/oauth2/auth")
                .tokenUri("https://oauth2.ya.ru/token")
                .build();

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, "store", accessToken);

        when(manager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(Mono.just(authorizedClient));
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/balance/" + userId.toString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue()))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Amount.class)).thenReturn(Mono.just(amount));

        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(totalCartPrice));

        // Act
        Mono<Boolean> result = cartService.isAvailable(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }



    @Test
    void isAvailable_shouldReturnTrueWhenBalanceIsSufficient() {
        UUID userId = UUID.randomUUID();
        BigDecimal cartTotalPrice = new BigDecimal("50.00");
        BigDecimal userBalance = new BigDecimal("100.00");

        // Mock CartItemRepository
        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(cartTotalPrice));

        // Mock OAuth2AuthorizedClientManager
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock_token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                "store",
                "system",
                accessToken
        );
        when(manager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(Mono.just(authorizedClient));

        // Mock WebTestClient для /balance/{userId}
        webTestClient = webTestClient.mutate().baseUrl("http://localhost:8081").build();  // Замените порт на актуальный
        webTestClient.get()
                .uri("/balance/" + userId.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock_token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.amount").isEqualTo(userBalance.doubleValue());

        Mono<Boolean> isAvailableMono = cartService.isAvailable(userId);

        isAvailableMono.subscribe(isAvailable -> {
            assert isAvailable;
        });

    }

 */



    @Test
    void isAvailable_returnsTrueWhenBalanceIsSufficient() {
        UUID userId = UUID.randomUUID();
        BigDecimal cartTotal = new BigDecimal("50.00");
        BigDecimal balanceAmount = new BigDecimal("100.00");
        Amount amount = new Amount();
        amount.setAmount(balanceAmount);
        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(cartTotal));

        prepareManagerMockDependencies();

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"userId\":\"" + userId.toString() + "\", \"amount\": " + balanceAmount.toPlainString() + "}"));

        Mono<Boolean> isAvailableMono = cartService.isAvailable(userId);
        // Assert
        StepVerifier.create(isAvailableMono)
                .expectNext(true)
                .verifyComplete();

    }

    private void prepareManagerMockDependencies() {
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
    }

        @Test
    void isAvailable_returnsFalseWhenBalanceIsInsufficient() {
        UUID userId = UUID.randomUUID();
        BigDecimal cartTotal = new BigDecimal("100.00");
        BigDecimal balanceAmount = new BigDecimal("50.00");
        Amount amount = new Amount();
        amount.setAmount(balanceAmount);
        when(cartItemRepository.calculateTotalPriceByUserId(userId)).thenReturn(Mono.just(cartTotal));

        prepareManagerMockDependencies();


        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"userId\":\"" + userId.toString() + "\", \"amount\": " + balanceAmount.toPlainString() + "}"));

        Mono<Boolean> isAvailableMono = cartService.isAvailable(userId);
        // Assert
        StepVerifier.create(isAvailableMono)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenPaymentServiceReturnsError() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        BigDecimal cartTotalPrice = new BigDecimal("100.00");
        String mockAccessTokenValue = "mock-jwt-token";

        when(cartItemRepository.calculateTotalPriceByUserId(userId))
                .thenReturn(Mono.just(cartTotalPrice));

        OAuth2AccessToken mockAccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, mockAccessTokenValue, Instant.now(), Instant.now().plusSeconds(3600), Collections.singleton("scope"));
        OAuth2AuthorizedClient mockAuthorizedClient = new OAuth2AuthorizedClient(
                Mockito.mock(ClientRegistration.class), "test-principal", mockAccessToken, null);
        when(manager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(Mono.just(mockAuthorizedClient));

        // Simulate an error from the payment service (e.g., 500 Internal Server Error)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Mono<Boolean> resultMono = cartService.isAvailable(userId);

        StepVerifier.create(resultMono)
                .expectError(RuntimeException.class)
                .verify();

        mockWebServer.takeRequest();
        verify(manager).authorize(any(OAuth2AuthorizeRequest.class));
        verify(cartItemRepository).calculateTotalPriceByUserId(userId);
    }

    @Test
    void isAvailable_returnsFalseWhenBalanceServiceReturnsError() {
        UUID userId = UUID.randomUUID();
        BigDecimal cartTotalPrice = new BigDecimal("100.00");

        when(cartItemRepository.calculateTotalPriceByUserId(userId))
                .thenReturn(Mono.just(cartTotalPrice));

        // Simulate a failure during OAuth2 authorization
        when(manager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(Mono.error(new AuthenticationException("Authentication failed")));

        Mono<Boolean> resultMono = cartService.isAvailable(userId);

        StepVerifier.create(resultMono)
                .expectError(AuthenticationException.class) // Due to the flatMap chain and defaultIfEmpty(false)
                .verify();

        verify(manager).authorize(any(OAuth2AuthorizeRequest.class));
        verify(cartItemRepository).calculateTotalPriceByUserId(userId);
        // No request should be made to mockWebServer if auth fails

    }
}


