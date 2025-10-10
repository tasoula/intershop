package io.github.tasoula.intershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Отключение CSRF-защиты
                //.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .authorizeExchange(// Аналог authorizeHttpRequests()
                        exchanges -> {
                            exchanges
                                    .pathMatchers("/cart/**", "/orders/**").hasRole("USER")
                                    .pathMatchers("/catalog/products/new").hasRole("ADMIN")
                                    .pathMatchers("/css/**", "/js/**").permitAll()
                                    .pathMatchers("/catalog/**", "/login", "/register", "/test/**").permitAll()
                                    .anyExchange().authenticated();
                        }
                )
                // Поддержка HTTP Basic аутентификации
                // .httpBasic()

                // Форма логина для пользователей
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(successHandler())
                      //  .permitAll()
                )
                // Вход через OAuth 2.0 провайдеров
                // .oauth2Login()
                //
                //.logout(logout -> logout.logoutUrl("/"))

                // Настройка security-заголовков
              //  .headers(headers -> headers
              //          .frameOptions().disable()
             //  )
                .anonymous(anonymous -> anonymous
                        .principal("guestUser")
                        .authorities("ROLE_GUEST")
                        .key("uniqueAnonymousKey")
                )
                // Настройка обработки ошибок
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler((exchange, denied) ->
                                Mono.error(new AccessDeniedException("Access Denied")))
                )
                .build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler successHandler() {
        return (webFilterExchange, authentication) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders().setLocation(URI.create("/catalog/items"));
            return exchange.getResponse().setComplete();
        };
    }
}
