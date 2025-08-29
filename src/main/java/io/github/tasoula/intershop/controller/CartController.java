package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.enums.CartAction;
import io.github.tasoula.intershop.interceptor.CookieConstants;
import io.github.tasoula.intershop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }


    @GetMapping("/items")
    public String viewCart(HttpServletRequest request, Model model) {
        UUID userId = UUID.fromString((String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME));
        List<ProductDto> items = service.findByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("total", service.calculateTotalPriceByUserId(userId));
        model.addAttribute("empty", items.isEmpty());
        return "cart.html";
    }

    @GetMapping("total")
    private ResponseEntity<BigDecimal> getTotal(HttpServletRequest request){
        UUID userId = UUID.fromString((String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME));
        return ResponseEntity.ok(service.calculateTotalPriceByUserId(userId));
    }

    @GetMapping("is_empty")
    private ResponseEntity<Boolean> isEmpty(HttpServletRequest request){
        UUID userId = UUID.fromString((String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME));
        return ResponseEntity.ok(service.isEmpty(userId));
    }

    @PostMapping("items/{id}")
    public ResponseEntity<Integer> changeProductQuantityInCart2(HttpServletRequest request,
                                                               @PathVariable("id") UUID productId,
                                                               @RequestParam("action") CartAction action) {
        UUID userId = UUID.fromString((String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME));

        int newQuantity = 0;
        switch (action){
            case PLUS -> newQuantity = service.changeProductQuantityInCart(userId, productId, 1);
            case MINUS -> newQuantity = service.changeProductQuantityInCart(userId, productId, -1);
            case DELETE -> service.deleteCartItem(userId, productId);
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(newQuantity);
    }
}
