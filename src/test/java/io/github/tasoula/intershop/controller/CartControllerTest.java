package io.github.tasoula.intershop.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

//@WebMvcTest(value = CartController.class,
  //      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class CartControllerTest {

  /*  @Value("${cookie.user.id.name}")
    private String cookieName;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CartService cartService;
    @MockitoBean
    UserInterceptor userInterceptor;

    @Test
    void viewCart_shouldReturnCartViewWithItemsAndTotal() throws Exception {
        UUID userId = UUID.randomUUID();
        List<ProductDto> items = List.of(new ProductDto(
                                                UUID.randomUUID(),
                                                "Product 1",
                                                "Description 1",
                                                BigDecimal.valueOf(100.0),
                                                5,
                                                2));
        BigDecimal total = BigDecimal.valueOf(200.0);

        when(cartService.findByUserId(userId)).thenReturn(items);
        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(total);

        mockMvc.perform(get("/cart/items")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart.html"))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("empty", false))
                .andExpect(model().attribute("total", total));
    }


    @Test
    void viewCart_shouldReturnCartViewWithEmptyCart() throws Exception {
        UUID userId = UUID.randomUUID();
        List<ProductDto> items = List.of();
        BigDecimal total = BigDecimal.ZERO;

        when(cartService.findByUserId(userId)).thenReturn(items);
        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(total);
        mockMvc.perform(get("/cart/items")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart.html"))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("total", total))
                .andExpect(model().attribute("empty", true));
    }

    @Test
    void changeProductQuantityInCart_shouldDeleteCartItem() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(post("/cart/items/{id}", productId)
                        .param("action", CartAction.DELETE.name())
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("0"));
    }


    @Test
    void changeProductQuantityInCart_shouldIncrementQuantity() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int newQuantity = 2;

        when(cartService.changeProductQuantityInCart(eq(userId), eq(productId), eq(1))).thenReturn(newQuantity);

        // Act & Assert
        mockMvc.perform(post("/cart/items/{id}", productId)
                        .param("action", CartAction.PLUS.name())
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("2"));
    }

    @Test
    void changeProductQuantityInCart_shouldDecrementQuantity() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int newQuantity = 1;

        when(cartService.changeProductQuantityInCart(eq(userId), eq(productId), eq(-1))).thenReturn(newQuantity);
        mockMvc.perform(post("/cart/items/{id}", productId)
                        .param("action", CartAction.MINUS.name())
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("1"));
    }

    @Test
    void getTotal_ReturnsTotalPriceFromCartService() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        BigDecimal totalPrice = BigDecimal.TEN;

        when(cartService.calculateTotalPriceByUserId(userId)).thenReturn(totalPrice);
        when(userInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/cart/total")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(totalPrice.toString()));

        Mockito.verify(cartService).calculateTotalPriceByUserId(userId);
    }

    @Test
    void isEmpty_ReturnsTrueIfCartIsEmpty() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(cartService.isEmpty(userId)).thenReturn(true);
        when(userInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/cart/is_empty")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        Mockito.verify(cartService).isEmpty(userId);
    }

    @Test
    void isEmpty_ReturnsFalseIfCartIsNotEmpty() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(cartService.isEmpty(userId)).thenReturn(false);
        when(userInterceptor.preHandle(any(), any(), any())).thenReturn(true);


        // Act & Assert
        mockMvc.perform(get("/cart/is_empty")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        Mockito.verify(cartService).isEmpty(userId);
    }

   */
}