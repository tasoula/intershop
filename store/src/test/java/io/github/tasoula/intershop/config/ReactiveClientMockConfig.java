package io.github.tasoula.intershop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class ReactiveClientMockConfig {

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        return org.mockito.Mockito.mock(ReactiveClientRegistrationRepository.class);
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientService authorizedClientService() {
        return org.mockito.Mockito.mock(ReactiveOAuth2AuthorizedClientService.class);
    }
}
