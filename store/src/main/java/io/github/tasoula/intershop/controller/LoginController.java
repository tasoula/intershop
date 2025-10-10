package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.UserRegistrationDto;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Controller
public class LoginController {

    private final UserService userDetailService;

    public LoginController(UserService userDetailService) {
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
    public Mono<String> create(@ModelAttribute UserRegistrationDto userRegistrationDto, Model model) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User user = new User();
        user.setUserName(userRegistrationDto.getUsername());
        user.setPassword(encoder.encode(userRegistrationDto.getPassword()));
        List<String> authorities = userRegistrationDto.isAdmin() ? List.of("ROLE_USER")
                : List.of("ROLE_ADMIN", "ROLE_USER");

        return userDetailService.saveUser(user, authorities)
                .flatMap(r -> {
                    model.addAttribute("newUser", true); //todo
                    return Mono.just("redirect:/login?newUser=true");
                })
                .switchIfEmpty(Mono.just("redirect:/register?alreadyExists=true"));
              //  .thenReturn(
              //  ResponseEntity.status(HttpStatus.FOUND)
              //          .location(URI.create("/login"))
              //          .build()
        //);
    }
}
