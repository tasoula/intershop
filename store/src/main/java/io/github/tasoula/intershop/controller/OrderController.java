package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<String> show(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono,
                             Model model) {
        return userDetailsMono.cast(User.class)
                .switchIfEmpty(Mono.just(new User()))
                .flatMap(user -> {
                    UUID userId = user.getId();

                    return service.getByUserId(userId)
                            .collectList()
                            .flatMap(items -> {
                                model.addAttribute("orders", items);
                                return Mono.just("orders.html");
                            });
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
    public Mono<String> createOrder(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono,
                                    Model model) {
        return userDetailsMono.cast(User.class)
                .switchIfEmpty(Mono.just(new User()))
                .flatMap(user -> {
                    return service.createOrder(user.getId())
                            .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true")
                            .switchIfEmpty(Mono.just("redirect:/cart/items"));
                });
    }
}
