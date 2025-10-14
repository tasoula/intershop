package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.github.tasoula.intershop.interceptor.CookieConst.USER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/*@WebFluxTest(value = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserInterceptor.class))
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductService productService;

    @Test
    void show_testRedirect() {

        webTestClient.get()
                .uri("/catalog")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/catalog/items");
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
        product1.setDescription("Product A Description");
        product1.setStockQuantity(9);
        product1.setQuantity(5);

        ProductDto product2 = new ProductDto();
        product2.setId(UUID.randomUUID());
        product2.setTitle("Product B");
        product2.setPrice(BigDecimal.valueOf(20.0));
        product1.setDescription("Product B Description");
        product1.setStockQuantity(10);
        product1.setQuantity(6);

        List<ProductDto> productList = List.of(product1, product2);
        Mono<Page<ProductDto>> productPage = Mono.just(new PageImpl<>(productList,
                PageRequest.of(pageNumber, pageSize,
                        Sort.by(ProductController.TITLE).ascending()),
                productList.size()));

        when(productService.findAll(any(UUID.class), eq(search), any(Pageable.class))).thenReturn(productPage);

        // Добавляем атрибут в запрос (имитируем работу userInterceptor)
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/catalog/items")
                        .queryParam("search", search)
                        .queryParam("sort", sort)
                        .queryParam("pageSize", String.valueOf(pageSize))
                        .queryParam("pageNumber", String.valueOf(pageNumber))
                        .build())
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());

                    assertTrue(body.contains("<title>Витрина товаров</title>"), "page title not found in template");
                    assertTrue(body.contains(product1.getTitle()), "product1 not found in template");
                    assertTrue(body.contains(product2.getTitle()), "product2 not found in template");
                    assertTrue(body.contains(search), "Search attribute not found in template");
                    assertTrue(body.contains(sort), "Sort attribute not found in template");
                });
    }

    @Test
    void showItems_withInvalidPageSize_defaultsTo10() {
        UUID userId = UUID.randomUUID();
        // Arrange
        int invalidPageSize = -1;
        int defaultPageSize = 10;

        when(productService.findAll(eq(userId), any(), any(Pageable.class)))
                .thenReturn(Mono.just(new PageImpl<>(java.util.List.of(), PageRequest.of(0, defaultPageSize), 0)));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/catalog/items")
                        .queryParam("pageSize", invalidPageSize)
                        .build())
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showItemById_withValidId_returnsItemHtml() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ProductDto productDto = new ProductDto(itemId, "title", "desc", BigDecimal.TEN, 10, 4);
        when(productService.findById(userId, itemId)).thenReturn(Mono.just(productDto));

        webTestClient.get()
                .uri("/catalog/items/{id}", itemId)
                .cookie(USER_ID, userId.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());
                    assertTrue(body.contains("<title>Витрина товаров</title>"), "page title not found in template");
                    assertTrue(body.contains(productDto.getTitle()), "product title not found in template");
                    assertTrue(body.contains(productDto.getDescription()), "product description not found in template");
                });
    }

    @Test
    void newProductForm_returnsNewProductHtml() {
        // Act
        webTestClient.get()
                .uri("/catalog/products/new")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                    String body = new String(result.getResponseBodyContent());
                    assertTrue(body.contains("<title>Добавить новый продукт</title>"), "page title not found in template");
                });


    }
}

 */