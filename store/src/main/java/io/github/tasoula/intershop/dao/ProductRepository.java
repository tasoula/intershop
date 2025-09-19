package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, UUID> {

    @Query("SELECT image_path FROM t_products WHERE id = :id")
    Mono<String> findImgPathById(UUID id);

    Flux<Product> findByStockQuantityGreaterThan(int quantity, Pageable pageable);

    Flux<Product> findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(String title, String description, int i, Pageable pageable);
}

