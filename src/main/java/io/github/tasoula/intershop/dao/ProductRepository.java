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
//----------------------

    // R2DBC не поддерживает findAllBy... напрямую.  Нужно использовать @Query и SQL.
    // Пример:
    //   @Query("SELECT * FROM t_products WHERE stock_quantity > :stockQuantity LIMIT :limit OFFSET :offset") //Подставьте фактическое имя колонки 'stock_quantity' и таблицы 'product'
    //   Mono<Page<Product>> findAllByStockQuantityGreaterThan(int stockQuantity, long limit, long offset);


    //R2DBC также не поддерживает findBy...ContainingIgnoreCase напрямую.  Нужно использовать @Query и SQL.
    //Пример:
    //   @Query("SELECT * FROM product WHERE (LOWER(title) LIKE LOWER(:titleLike) OR LOWER(description) LIKE LOWER(:descriptionLike)) AND stock_quantity > :stockQuantity LIMIT :limit OFFSET :offset") //Подставьте фактические имена колонок 'title', 'description' и 'stock_quantity' и таблицы 'product'
    //   Mono<Page<Product>> findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(String titleLike, String descriptionLike, int stockQuantity, long limit, long offset);


 /*  default Mono<Page<Product>> findAllByStockQuantityGreaterThan(int i, Pageable pageable) {
        return findAllByStockQuantityGreaterThan(i, pageable.getPageSize(), pageable.getOffset())
                .zipWith(countByStockQuantityGreaterThan(i), (products, count) -> {
                    return new org.springframework.data.domain.PageImpl<>(products.collectList().block(), pageable, count);
                });
    }


    default Mono<Page<Product>> findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(String lowerCase, String lowerCase1, int i, Pageable pageable) {
        String titleLike = "%" + lowerCase + "%";
        String descriptionLike = "%" + lowerCase1 + "%";
        return findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(titleLike, descriptionLike, i, pageable.getPageSize(), pageable.getOffset())
                .zipWith(countByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(lowerCase,lowerCase1,i), (products, count) -> {
                    return new org.springframework.data.domain.PageImpl<>(products.collectList().block(), pageable, count);
                });
    }

    @Query("SELECT COUNT(*) FROM product WHERE stock_quantity > :stockQuantity")//Подставьте фактическое имя колонки 'stock_quantity' и таблицы 'product'
    Mono<Long> countByStockQuantityGreaterThan(int stockQuantity);

    @Query("SELECT COUNT(*) FROM product WHERE (LOWER(title) LIKE LOWER(:titleLike) OR LOWER(description) LIKE LOWER(:descriptionLike)) AND stock_quantity > :stockQuantity") //Подставьте фактические имена колонок 'title', 'description' и 'stock_quantity' и таблицы 'product'
    Mono<Long> countByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(String titleLike, String descriptionLike, int stockQuantity);


    default Mono<Long> countByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(String lowerCase,String lowerCase1, int i){
        String titleLike = "%" + lowerCase + "%";
        String descriptionLike = "%" + lowerCase1 + "%";
        return countByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(titleLike, descriptionLike,i);
    }

  */
}

