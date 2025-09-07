package io.github.tasoula.intershop.config;

import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.resolvers.UserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

@Configuration
@EnableScheduling
public class WebConfig //implements WebMvcConfigurer
{

    @Value("${cookie.user.id.name}")
    private String cookieName;
/*
    private final UserInterceptor userInterceptor;

    public WebConfig(UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor).addPathPatterns("/**"); // Применяем ко всем URL
        //registry.addInterceptor(userInterceptor).addPathPatterns("/cart/**", "/orders/**"); // Применяем только к корзине и заказам
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIdArgumentResolver(cookieName));
    }

 */
}