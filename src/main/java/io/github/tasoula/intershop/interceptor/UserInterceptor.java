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

            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Strict");

            /*
                todo:
                 Использовать CSP (Content Security Policy) для предотвращения выполнения вредоносных скриптов
• Реализовать проверку CSRF-токенов. (когда будет spring sequrity)
• Использовать HTTPS для шифрования трафика.
•  Реализация:
  1. Получите SSL/TLS сертификат: От доверенного удостоверяющего центра (Certificate Authority, CA). Let's Encrypt предоставляет бесплатные сертификаты.
  2. Настройте ваш сервер: Настройте ваш веб-сервер (например, Apache, Nginx, Tomcat) для использования HTTPS. Инструкции зависят от вашего сервера. Для Tomcat, это включает настройку коннектора с scheme="https" и secure="true".
  3. Перенаправление HTTP на HTTPS: Настройте перенаправление всех HTTP-запросов на HTTPS. Это можно сделать в вашем веб-сервере или в Spring Boot приложении с помощью Filter.
• Установить флаг Secure для cookies, чтобы они отправлялись только через защищённые каналы.
• Использовать механизм обновления токенов (refresh tokens). (когда будет spring sequrity)
             */

            response.addCookie(cookie);
        }

        // Добавляем ID пользователя в атрибуты запроса, чтобы он был доступен в контроллерах
        request.setAttribute("userId", userId);

        return true; // Продолжаем обработку запроса
    }
}
