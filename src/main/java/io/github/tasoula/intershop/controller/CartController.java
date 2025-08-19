package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductCatalogItemDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    //todo как-то разделить продукты и cart_item здесь и в сервисах
    private final ProductService service;

    public CartController(ProductService service) {
        this.service = service;
    }


    @GetMapping("/items")
    public String viewCart(HttpServletRequest request, Model model) {
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        List<ProductCatalogItemDto> items = service.findById(userId);
        model.addAttribute("items", items);
        return "cart.html";
    }

    @PostMapping("items/{id}")
    public ResponseEntity<Integer> changeCartQuantity(HttpServletRequest request,
                                                      @PathVariable("id") UUID id,
                                                      @RequestParam("action") String action) {
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        int newQuantity = service.changeCartQuantity(userId, id, action.equals("plus") ? 1 : -1); // Get the new quantity from the service
        return ResponseEntity.ok(newQuantity); // Return the updated quantity
    }
}
