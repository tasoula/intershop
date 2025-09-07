package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.annotations.UserId;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {
 /*   private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public String show(@UserId UUID userId,
                            Model model) {
        model.addAttribute("orders", service.getByUserId(userId));
        return "orders.html";
    }

    @GetMapping("{id}")
    public String showOrder(@PathVariable("id") UUID id,
                            @RequestParam(name = "newOrder", required = false, defaultValue = "false") boolean isNew,
                            Model model) {
        model.addAttribute("order", service.getById(id).orElseThrow(() -> new ResourceNotFoundException("Заказ " + id + " не найден")));
        model.addAttribute("newOrder", isNew);
        return "order.html";
    }

    @PostMapping("new")
    public String createOrder(@UserId UUID userId, Model model) {
        Optional<UUID> orderId = service.createOrder(userId);
        return orderId.map(uuid -> "redirect:/orders/" + uuid + "?newOrder=true").orElse("redirect:/cart/items");
    }

  */
}
