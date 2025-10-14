package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import java.net.URI;

@Controller
@RequestMapping("/test")
public class ShowController {
    @GetMapping("user")
    public Mono<String> showUser(@AuthenticationPrincipal Mono<UserDetails> userDetailsMono, Model model) {
        return userDetailsMono.map(UserDetails::getUsername)
                .doOnNext(username -> {
                    model.addAttribute("username", username);

                })
                .thenReturn("test.html");
    }

    @GetMapping("context")
    public Mono<String> showUser2(Model model) {
       return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication) // Получаем объект Authentication
                .filter(Authentication::isAuthenticated)  // Фильтруем, если аутентификация не прошла
                .map(Authentication::getPrincipal)         // Получаем principal (сущность пользователя)
                .cast(User.class)                   // Преобразуем в UserDetails
                .map(User::getUsername).doOnNext(username -> {
                    model.addAttribute("username", username);
                })
                .thenReturn("test.html");



    /*    return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .doOnNext(auth -> {
                    model.addAttribute("username", auth.getPrincipal());
                })
                .thenReturn("test.html");

     */


    }
}
