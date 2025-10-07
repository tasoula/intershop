package io.github.tasoula.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping()
    public Mono<String> login() {
        return Mono.just("login.html"); // Имя HTML-шаблона
    }

    @PostMapping
    public Mono<Void> authentificate(){
        return Mono.empty();
    }
}
