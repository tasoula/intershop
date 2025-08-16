package io.github.tasoula.intershop.interceptor;

import io.github.tasoula.intershop.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final String USER_ID_COOKIE_NAME = "userId";

    private final UserService service;

    public UserInterceptor(UserService service) {
        this.service = service;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<Cookie> userIdCookie = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> USER_ID_COOKIE_NAME.equals(cookie.getName()))
                .findFirst();

        String userId = userIdCookie.map(Cookie::getValue).orElse(null);

        if (userId == null) {
            userId = service.createUser().toString();
            Cookie cookie = new Cookie(USER_ID_COOKIE_NAME, userId);
            cookie.setMaxAge(3600 * 24 * 30); // 30 days
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        // Добавляем ID пользователя в атрибуты запроса, чтобы он был доступен в контроллерах
        request.setAttribute("userId", userId);

        return true; // Продолжаем обработку запроса
    }
}
