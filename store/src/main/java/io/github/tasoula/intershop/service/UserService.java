package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.AuthorityRepository;
import io.github.tasoula.intershop.dao.UserAuthorityBindRepository;
import io.github.tasoula.intershop.dao.UserRepository;
import io.github.tasoula.intershop.model.Authority;
import io.github.tasoula.intershop.model.UserAuthorityBind;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import io.github.tasoula.intershop.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final UserAuthorityBindRepository userAuthorityBindRepository;

    public UserService(UserRepository repository, AuthorityRepository authorityRepository, UserAuthorityBindRepository userAuthorityBindRepository) {
        this.userRepository = repository;
        this.authorityRepository = authorityRepository;
        this.userAuthorityBindRepository = userAuthorityBindRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUserName(username)
                .flatMap(user -> {
                    return userAuthorityBindRepository.findByUserId(user.getId())
                            .flatMap(userAuthority -> authorityRepository.findById(userAuthority.getAuthorityId()))
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .collectList()
                            .map(authorities -> {
                                user.setAuthorities(authorities);
                                return user;
                            });
                });
    }


    @Transactional
    public Mono<UserDetails> saveUser(User user, List<String> authorityNames) {
        // 1. Сохраняем пользователя
        return userRepository.save(user)
                .flatMap(savedUser -> {
                    // 2. Находим или создаем authorities
                    Flux<Authority> authoritiesFlux = Flux.fromIterable(authorityNames)
                            .flatMap(authorityName -> authorityRepository.findByAuthority(authorityName)
                                    .switchIfEmpty(authorityRepository.save(new Authority(null, authorityName)))); //если authority не существует, создаем его
                    //.defaultIfEmpty(new Authority(null, authorityName))));

                    // 3. Сохраняем связи между пользователем и authorities в t_users_authorities
                    return authoritiesFlux.collectList()
                            .flatMap(authorities -> {
                                Flux<UserAuthorityBind> userAuthoritiesFlux = Flux.fromIterable(authorities)
                                        .map(authority -> new UserAuthorityBind(null, savedUser.getId(), authority.getId()));

                                return userAuthorityBindRepository.saveAll(userAuthoritiesFlux)
                                        .collectList()
                                        .thenReturn(savedUser); // Возвращаем сохраненного пользователя
                            });
                })
                .flatMap(savedUser -> findByUsername(savedUser.getUsername()));//Заново получаем пользователя, но уже с ролями
    }
}
