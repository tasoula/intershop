package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.UserRegistrationDto;
import io.github.tasoula.intershop.service.StoreUserDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
public class LoginController {

    private final StoreUserDetailService userDetailService;

    public LoginController(StoreUserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @GetMapping("/login")
    public Mono<String> login(WebSession session) {
        return session.changeSessionId()
                .thenReturn("login.html");
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

    @GetMapping("/register")
    public Mono<String> register(WebSession session) {
        return session.changeSessionId()
                .thenReturn("register.html");
    }

    @PostMapping("/register")
    public Mono<String> create(@ModelAttribute UserRegistrationDto userRegistrationDto) {
        return userDetailService.create(userRegistrationDto.getUsername(), userRegistrationDto.getPassword())
                .map(user -> "redirect:/login?newUser=true")//todo доделать отображение нового пользователя
                .switchIfEmpty(Mono.just("redirect:/register?alreadyExists=true"));
              //  .thenReturn(
              //  ResponseEntity.status(HttpStatus.FOUND)
              //          .location(URI.create("/login"))
              //          .build()
        //);
    }
}
