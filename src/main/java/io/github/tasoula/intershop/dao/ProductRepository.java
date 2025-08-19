package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE %:search% OR LOWER(p.description) LIKE %:search%")
    Page<Product> findByTitleContainingOrDescriptionContainingIgnoreCase(@Param("search") String search, Pageable pageable);

    @Query("SELECT p.imgPath FROM Product p WHERE p.id = :id")
    String findImgPathById(@Param("id") UUID id);
}

