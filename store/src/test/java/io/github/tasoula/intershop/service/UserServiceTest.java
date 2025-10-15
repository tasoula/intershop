package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.AuthorityRepository;
import io.github.tasoula.intershop.dao.UserAuthorityBindRepository;
import io.github.tasoula.intershop.dao.UserRepository;
import io.github.tasoula.intershop.model.Authority;
import io.github.tasoula.intershop.model.User;
import io.github.tasoula.intershop.model.UserAuthorityBind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private UserAuthorityBindRepository userAuthorityBindRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Authority testAuthority;
    private UserAuthorityBind testUserAuthorityBind;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUserName("testUser");
        testUser.setPassword("password");

        testAuthority = new Authority();
        testAuthority.setId(UUID.randomUUID());
        testAuthority.setAuthority("ROLE_USER");

        testUserAuthorityBind = new UserAuthorityBind();
        testUserAuthorityBind.setId(UUID.randomUUID());
        testUserAuthorityBind.setUserId(testUser.getId());
        testUserAuthorityBind.setAuthorityId(testAuthority.getId());
    }

    @Test
    void findByUsername_userExists_returnsUserDetails() {
        when(userRepository.findByUserName(anyString())).thenReturn(Mono.just(testUser));
        when(userAuthorityBindRepository.findByUserId(testUser.getId())).thenReturn(Flux.fromIterable(List.of(testUserAuthorityBind)));
        when(authorityRepository.findById(testAuthority.getId())).thenReturn(Mono.just(testAuthority));

        Mono<UserDetails> userDetailsMono = userService.findByUsername("testUser");

        StepVerifier.create(userDetailsMono)
                .assertNext(userDetails -> {
                    assert userDetails.getUsername().equals("testUser");
                    assert userDetails.getAuthorities().size() == 1;
                    assert userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_userDoesNotExist_returnsEmptyMono() {
        when(userRepository.findByUserName(anyString())).thenReturn(Mono.empty());

        Mono<UserDetails> userDetailsMono = userService.findByUsername("nonExistentUser");

        StepVerifier.create(userDetailsMono)
                .verifyComplete(); // Expect the Mono to be empty
    }


    @Test
    void saveUser_newUserWithAuthorities_userSavedAndAuthoritiesBound() {
        UUID userId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(userId);
        newUser.setUserName("newUser");
        newUser.setPassword("password");

        UUID authorityId = UUID.randomUUID();
        Authority newAuthority = new Authority();
        newAuthority.setId(authorityId);
        newAuthority.setAuthority("ROLE_ADMIN");
        List<String> authorityNames = Collections.singletonList("ROLE_ADMIN");

        UUID userAuthorityBindId = UUID.randomUUID();
        UserAuthorityBind userAuthorityBind = new UserAuthorityBind();
        userAuthorityBind.setId(userAuthorityBindId);
        userAuthorityBind.setUserId(userId);
        userAuthorityBind.setAuthorityId(authorityId);
        Flux<UserAuthorityBind> userAuthorityBindFlux = Flux.fromIterable(List.of(userAuthorityBind));

        when(userRepository.save(newUser)).thenReturn(Mono.just(newUser));
        when(authorityRepository.findByAuthority("ROLE_ADMIN")).thenReturn(Mono.empty());
        when(authorityRepository.save(any(Authority.class))).thenReturn(Mono.just(newAuthority));
        when(userAuthorityBindRepository.saveAll((Flux<UserAuthorityBind>) any())).thenReturn(userAuthorityBindFlux);
        when(userRepository.findByUserName("newUser")).thenReturn(Mono.just(newUser));
        when(userAuthorityBindRepository.findByUserId(userId)).thenReturn(userAuthorityBindFlux);
        when(authorityRepository.findById(authorityId)).thenReturn(Mono.just(newAuthority));

        Mono<UserDetails> userDetailsMono = userService.saveUser(newUser, authorityNames);

        StepVerifier.create(userDetailsMono)
                .assertNext(userDetails -> {
                    assert userDetails.getUsername().equals("newUser");
                    assert userDetails.getAuthorities().size() == 1;
                    assert userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")); // The role from the mock findByUsername call
                })
                .verifyComplete();
    }
}
