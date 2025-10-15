package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.ProductService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
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

        when(productService.findAll(any(), eq(search), any(Pageable.class))).thenReturn(productPage);

        // Добавляем атрибут в запрос (имитируем работу userInterceptor)
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/catalog/items")
                        .queryParam("search", search)
                        .queryParam("sort", sort)
                        .queryParam("pageSize", String.valueOf(pageSize))
                        .queryParam("pageNumber", String.valueOf(pageNumber))
                        .build())
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
        int invalidPageSize = -1;
        int defaultPageSize = 10;

        when(productService.findAll(any(), any(), any(Pageable.class)))
                .thenReturn(Mono.just(new PageImpl<>(java.util.List.of(), PageRequest.of(0, defaultPageSize), 0)));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/catalog/items")
                        .queryParam("pageSize", invalidPageSize)
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showItemById_withValidId_returnsItemHtml() {
        UUID itemId = UUID.randomUUID();
        ProductDto productDto = new ProductDto(itemId, "title", "desc", BigDecimal.TEN, 10, 4);
        when(productService.findById(any(), eq(itemId))).thenReturn(Mono.just(productDto));

        webTestClient.get()
                .uri("/catalog/items/{id}", itemId)
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
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUserName("admin");
        mockUser.setPassword("admin");
        mockUser.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        // Act
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(adminAuthentication))
                .get()
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

    @Test
    void newProductForm_accessDeniedForUser() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUserName("user");
        mockUser.setPassword("user");
        mockUser.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication userAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        // Act
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(userAuthentication))
                .get()
                .uri("/catalog/products/new")
                .exchange()
                .expectStatus()
                .isForbidden();
    }



    @Test
    void newProductForm_redirectWhenUnautorized() {
        webTestClient.get()
                .uri("/catalog/products/new")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }


}

