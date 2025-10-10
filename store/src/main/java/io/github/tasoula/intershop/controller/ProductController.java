package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/catalog")
public class ProductController {

    public static final String TITLE = "title";
    public static final String PRICE = "price";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping()
    public Mono<ResponseEntity<Void>> redirect() {
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/catalog/items"))
                .build());
    }

    @GetMapping("items")
    public Mono<String> showItems(
            @AuthenticationPrincipal Mono<UserDetails> userDetailsMono,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            Model model,
            ServerWebExchange exchange) { // ServerWebExchange для получения доступа к WebFlux контексту

        if (pageSize <= 0) pageSize = 10;
        if (pageNumber < 0) pageNumber = 0;

        Sort sortObj = switch (sort) {
            case "ALPHA" -> Sort.by(TITLE).ascending();
            case "PRICE" -> Sort.by(PRICE).ascending();
            default -> Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);

        return userDetailsMono.cast(User.class)
                .switchIfEmpty(Mono.just(new User()))
                .flatMap(user -> {
                    return service.findAll(user.getId(), search, pageable)
                            .doOnNext(productPage -> {
                                model.addAttribute("search", search);
                                model.addAttribute("sort", sort);
                                model.addAttribute("paging", productPage);
                                model.addAttribute("items", productPage.getContent());
                            })
                            .thenReturn("catalog.html");
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("-----Ошибка при обработке запроса items: {}", e.getMessage(), e);
                    model.addAttribute("errorMessage", "Произошла ошибка при загрузке данных.");
                    return Mono.just("oops.html");
                });

    }

    @GetMapping("items/{id}")
    public Mono<String> showItemById(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono, @PathVariable("id") UUID id, Model model) {
        return userDetailsMono.cast(User.class)
                .switchIfEmpty(Mono.just(new User()))
                .flatMap(user -> {
                    return service.findById(user.getId(), id)
                            .doOnNext(productDto -> {
                                model.addAttribute("item", productDto);
                            })
                            .thenReturn("item.html");
                });
    }


    @GetMapping("/products/new")
    public Mono<String> newProductForm(Model model) {
        return Mono.just("new-product");
    }

    @PostMapping(value = "products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> createProduct(@RequestPart("image") Mono<FilePart> image,
                                      @ModelAttribute ProductDto productDto) {
        return image.flatMap(filePart -> {
                    return service.createProduct(productDto, filePart)
                            .thenReturn("redirect:/catalog/items"); //  Возвращаем строку для редиректа
                })
                .onErrorResume(e -> {
                    System.err.println("Error during product creation: " + e.getMessage());
                    return Mono.just("redirect:/catalog/items?error=true"); // Или другой вид обработки ошибки
                });
    }

}