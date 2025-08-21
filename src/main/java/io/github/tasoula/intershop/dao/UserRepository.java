package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Product;
import io.github.tasoula.intershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    void deleteByCreatedAtBefore(Timestamp createdAt);
}
