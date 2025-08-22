package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final ProductService service;

    public OrderController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public String show(HttpServletRequest request,
                            Model model) {
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        model.addAttribute("orders", service.getByUserId(userId));
        return "orders.html";
    }

    @GetMapping("{id}")
    public String showOrder(@PathVariable("id") UUID id,
                            @RequestParam(name = "newOrder", required = false, defaultValue = "false") boolean isNew,
                            Model model) {
        model.addAttribute("order", service.getById(id).get());
        model.addAttribute("newOrder", isNew);
        return "order.html";
    }
}
