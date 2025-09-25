package io.github.tasoula.intershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${balance.service.url}")
    private String balanceServiceUrl;

    @Bean
    WebClient balanceWebClient(){
        return WebClient.builder().baseUrl(balanceServiceUrl).build();
    }
}

