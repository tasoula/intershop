package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<User, UUID> {
    void deleteByCreatedAtBefore(Timestamp createdAt);
}
