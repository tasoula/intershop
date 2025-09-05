package io.github.tasoula.intershop.dao;


import io.github.tasoula.intershop.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
}
