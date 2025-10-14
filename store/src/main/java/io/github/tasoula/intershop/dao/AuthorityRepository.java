package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Authority;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthorityRepository extends R2dbcRepository<Authority, UUID> {
    Mono<Authority> findByAuthority(String authority);
}
