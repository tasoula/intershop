package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.github.tasoula.intershop.interceptor.CoockieConst.USER_ID;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<String> show(@CookieValue(USER_ID) UUID userId,  Model model) {
        return service.getByUserId(userId)
                        .collectList()
                                .flatMap(items -> {
                                    model.addAttribute("orders", items);
                                    return Mono.just("orders.html");
                                });
    }

    @GetMapping("{id}")
    public Mono<String> showOrder(@PathVariable("id") UUID id,
                                  @RequestParam(name = "newOrder", required = false, defaultValue = "false") boolean isNew,
                                  Model model) {
        return service.getById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Заказ " + id + " не найден")))
                .flatMap(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", isNew);
                    return Mono.just("order.html");
                })
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    model.addAttribute("exception", ex);
                    return Mono.just("exceptions/not-found"); // Обрабатываем ResourceNotFoundException
                });
    }

    @PostMapping("new")
    public Mono<String> createOrder(@CookieValue(USER_ID) UUID userId, Model model) {
        return service.createOrder(userId)
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true")
                .switchIfEmpty(Mono.just("redirect:/cart/items"));
    }


}
