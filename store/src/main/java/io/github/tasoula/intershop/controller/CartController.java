package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.enums.CartAction;
import io.github.tasoula.intershop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static io.github.tasoula.intershop.interceptor.CookieConst.USER_ID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/items")
    public Mono<String> viewCart(@CookieValue(USER_ID) UUID userId, Model model) {
        return service.findByUserId(userId) // Assuming findByUserId returns a Flux<ProductDto> or a Mono<List<ProductDto>>
                .collectList() // collect all items into list
                .flatMap(items -> {
                    model.addAttribute("items", items);
                    return service.calculateTotalPriceByUserId(userId) // Assuming calculateTotalPriceByUserId returns a Mono<BigDecimal>
                            .flatMap(total -> {
                                model.addAttribute("total", total);
                                model.addAttribute("empty", items.isEmpty());
                                return Mono.just("cart.html"); // Return the view name as a Mono
                            });
                });
    }

    @GetMapping("total")
    private Mono<ResponseEntity<BigDecimal>> getTotal(@CookieValue(USER_ID) UUID userId){
        return service.calculateTotalPriceByUserId(userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("is_empty")
    private Mono<ResponseEntity<Boolean>> isEmpty(@CookieValue(USER_ID) UUID userId) {
        return service.isEmpty(userId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("items/{id}")
    public Mono<ResponseEntity<Integer>> changeProductQuantityInCart(
            @CookieValue(USER_ID) UUID userId,
            @PathVariable("id") UUID productId,
            @RequestParam("action") CartAction action
    ) {
        return switch (action) {
            case PLUS -> service.changeProductQuantityInCart(userId, productId, 1)
                    .map(ResponseEntity::ok);
            case MINUS -> service.changeProductQuantityInCart(userId, productId, -1)
                    .map(ResponseEntity::ok);
            case DELETE -> service.deleteCartItem(userId, productId)
                    .thenReturn(ResponseEntity.ok(0)); // Возвращаем OK с 0, т.к. quantity не возвращается при DELETE
        };
    }
}
