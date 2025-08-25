package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.config.WebConfig;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.interceptor.CookieConstants;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.OrderService;
import io.github.tasoula.intershop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    UserInterceptor userInterceptor;

    @Test
    void show_redirectsToItems() throws Exception {
        mockMvc.perform(get("/catalog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/items"));
    }

    @Test
    void showItems_returnsCatalogView() throws Exception {
        String search = "testSearch";
        String sort = "ALPHA";
        int pageSize = 2;
        int pageNumber = 1;
        UUID userId = UUID.randomUUID();

        ProductDto product1 = new ProductDto();
        product1.setId(UUID.randomUUID());
        product1.setTitle("Product A");
        product1.setPrice(BigDecimal.TEN);

        ProductDto product2 = new ProductDto();
        product2.setId(UUID.randomUUID());
        product2.setTitle("Product B");
        product2.setPrice(BigDecimal.valueOf(20.0));

        List<ProductDto> productList = List.of(product1, product2);
        Page<ProductDto> productPage = new PageImpl<>(productList, PageRequest.of(pageNumber, pageSize, Sort.by(ProductController.TITLE).ascending()), productList.size());

        when(productService.findAll(any(UUID.class), eq(search), any(Pageable.class))).thenReturn(productPage);
        when(userInterceptor.preHandle(any(HttpServletRequest.class), any(), any())).thenReturn(true);

        // Добавляем атрибут в запрос (имитируем работу userInterceptor)
        MockHttpServletRequestBuilder requestBuilder = get("/catalog/items")
                .param("search", search)
                .param("sort", sort)
                .param("pageSize", String.valueOf(pageSize))
                .param("pageNumber", String.valueOf(pageNumber));

        // Добавляем userId в атрибуты запроса.
        requestBuilder.requestAttr(CookieConstants.USER_ID_COOKIE_NAME, userId.toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("catalog.html"))
                .andExpect(model().attribute("search", search))
                .andExpect(model().attribute("sort", sort))
                .andExpect(model().attribute("paging", productPage))
                .andExpect(model().attribute("items", productList));
    }

    @Test
    void showItems_usesDefaultParameters() throws Exception {
        Page<ProductDto> productPage = new PageImpl<>(List.of());
        when(productService.findAll(eq(null), eq(null), any(Pageable.class))).thenReturn(productPage); // Указываем eq(null) для userId и search
        when(userInterceptor.preHandle(any(MockHttpServletRequest.class), any(), any())).thenReturn(true); // Используем MockHttpServletRequest

        mockMvc.perform(get("/catalog/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog.html"))
                .andExpect(model().attribute("search", (String) null)) //  (String) null
                .andExpect(model().attribute("sort", "NO"));
    }

    @Test
    void showItemById_returnsItemView() throws Exception {
        UUID itemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProductDto product = new ProductDto();
        product.setId(itemId);
        product.setTitle("Test Product");
        product.setDescription("Description of the product");
        product.setPrice(BigDecimal.TEN);

        when(productService.findById(userId, itemId)).thenReturn(product);
        when(userInterceptor.preHandle(any(HttpServletRequest.class), any(), any())).thenReturn(true);


        mockMvc.perform(get("/catalog/items/{id}", itemId)
                        .requestAttr(CookieConstants.USER_ID_COOKIE_NAME, userId.toString())) // Simulate request attribute set by interceptor
                .andExpect(status().isOk())
                .andExpect(view().name("item.html"))
                .andExpect(model().attribute("item", product));
    }

}