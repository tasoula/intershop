package io.github.tasoula.intershop.interceptor;

import io.github.tasoula.intershop.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class UserInterceptor implements WebFilter {

    @Value("${cookie.max.age.seconds}")
    private int cookieMaxAge;

    private final UserService service;

    public UserInterceptor(UserService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpCookie cookieUser = exchange.getRequest().getCookies().getFirst(CoockieConst.USER_ID);
        if(cookieUser!=null) {
            String userId = cookieUser.getValue();
            if (userId != null) {
                exchange.getAttributes().put(CoockieConst.USER_ID, userId); // Сохраняем userId в атрибутах запроса
                return Mono.just(userId).then(chain.filter(exchange));
            }
        }
        else {
            return service.createUser()
                    .map(userId -> {
                        exchange.getAttributes().put(CoockieConst.USER_ID, userId.toString()); // Сохраняем userId в атрибутах запроса
                        ResponseCookie cookie = ResponseCookie.from(CoockieConst.USER_ID, userId.toString())
                                .path("/")
                                .httpOnly(true)
                                .maxAge(cookieMaxAge)
                                .sameSite("Strict")
                                .build();
                        exchange.getResponse().addCookie(cookie);
                        return userId;
                    })
                    .flatMap(Mono::just)
                    .then(chain.filter(exchange));
        }

        return  Mono.empty();
    }
}
