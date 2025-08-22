package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
        List<ProductDto> items = service.findByUserId(userId);
        model.addAttribute("items", items);
        model.addAttribute("total", service.calculateTotalPriceByUserId(userId));
        model.addAttribute("empty", items.isEmpty());
        return "cart.html";
    }

    @PostMapping("items/{id}")
    public ResponseEntity<Integer> changeCartQuantity(HttpServletRequest request,
                                                      @PathVariable("id") UUID id,
                                                      @RequestParam("action") String action,
                                                      Model model) {
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));

        int newQuantity = 0;
        if(action.equals("delete")){
            service.deleteCartItem(userId, id);
        }
        else {
            newQuantity = service.changeCartQuantity(userId, id, action.equals("plus") ? 1 : -1); // Get the new quantity from the service
        }

        return ResponseEntity.ok(newQuantity); // Return the updated quantity
    }

    @GetMapping("total")
    private ResponseEntity<BigDecimal> getTotal(HttpServletRequest request){
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        return ResponseEntity.ok(service.calculateTotalPriceByUserId(userId));
    }

    @GetMapping("is_empty")
    private ResponseEntity<Boolean> isEmpty(HttpServletRequest request){
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        return ResponseEntity.ok(service.isEmpty(userId));
    }

    @PostMapping("buy")
    public String buy(HttpServletRequest request, Model model) {
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        Optional<UUID> orderId = service.createOrder(userId);
        return orderId.map(uuid -> "redirect:/orders/" + uuid + "?newOrder=true").orElse("redirect:/cart/items");
    }
}
