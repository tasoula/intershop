package io.github.tasoula.intershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${balance.service.url}") // Получаем базовый URL из application.yml
    private String balanceServiceUrl;

    @Bean
    WebClientCustomizer mediaTypeCustomizer() {
        return builder -> builder.baseUrl(balanceServiceUrl);
    }
}

