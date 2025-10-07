package io.github.tasoula.intershop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Отключение CSRF-защиты
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .authorizeExchange(// Аналог authorizeHttpRequests()
                        exchanges -> {
                            exchanges
                                    .pathMatchers("/cart/**", "/orders/**").hasRole("USER")
                                    .pathMatchers("/css/**", "/js/**").permitAll()
                                    .pathMatchers("/catalog/**", "/login").permitAll()
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
                .logout(logout -> logout.logoutUrl("/"))
                // Настройка security-заголовков
              //  .headers(headers -> headers
              //          .frameOptions().disable()
             //  )
                // Настройка обработки ошибок
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler((exchange, denied) ->
                                Mono.error(new AccessDeniedException("Access Denied")))
                )
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("password")) // Замените на надежный пароль!
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin")) // Замените на надежный пароль!
                .roles("ADMIN", "USER")
                .build();

        return new MapReactiveUserDetailsService(user, admin);
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
