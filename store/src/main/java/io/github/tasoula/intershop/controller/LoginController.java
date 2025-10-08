package io.github.tasoula.intershop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
@RequestMapping()
public class LoginController {

    @GetMapping("/login")
    public Mono<String> login() {
        return Mono.just("login.html"); // Имя HTML-шаблона
    }

    @GetMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(WebSession session) {
        return session.invalidate()
                .thenReturn(
                        ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create("/catalog/items"))
                                .build()
                );
    }
}
