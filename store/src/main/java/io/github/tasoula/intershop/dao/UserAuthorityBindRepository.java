package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.UserAuthorityBind;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserAuthorityBindRepository extends R2dbcRepository<UserAuthorityBind, UUID> {
    Flux<UserAuthorityBind> findByUserId(UUID userId);
}
