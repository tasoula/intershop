package io.github.tasoula.intershop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InternetShopApplicationTests {
	@MockitoBean
	private ReactiveClientRegistrationRepository clientRegistrationRepository;

	@MockitoBean
	private ReactiveOAuth2AuthorizedClientService authorizedClientService;
	@Test
	void contextLoads() {
	}

}
