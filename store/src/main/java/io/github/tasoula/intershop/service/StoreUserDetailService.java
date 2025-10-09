package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import io.github.tasoula.intershop.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class StoreUserDetailService implements ReactiveUserDetailsService {

    private final UserRepository repository;
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public StoreUserDetailService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByUserName(username);
    }

    public Mono<User> create(String username, String password){
        User user = new User();
        user.setUserName(username);
        user.setPassword(encoder.encode(password));
      //  user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return repository.save(user);
    }
}
