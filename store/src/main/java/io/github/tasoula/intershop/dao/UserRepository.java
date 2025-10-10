package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<User, UUID> {
    Mono<Void> deleteByCreatedAtBefore(Timestamp createdAt);
    Mono<User> findByUserName(String username);
}

