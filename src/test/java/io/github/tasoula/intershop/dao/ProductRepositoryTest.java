package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static io.github.tasoula.intershop.controller.ProductController.TITLE;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest extends SpringBootPostgreSQLBase{
    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setTitle("Amazing Product");
        product1.setDescription("This is an amazing product description.");
        product1.setPrice(new BigDecimal("25.00"));
        product1.setStockQuantity(10);
        product1.setImgPath("/images/product1.jpg");

        product2 = new Product();
        product2.setTitle("Another Product");
        product2.setDescription("Another great product.");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStockQuantity(5);
        product2.setImgPath("/images/product2.jpg");

        product3 = new Product();
        product3.setTitle("Low Stock Product");
        product3.setDescription("This product has low stock.");
        product3.setPrice(new BigDecimal("10.00"));
        product3.setStockQuantity(0);
        product3.setImgPath("/images/product3.jpg");

        productRepository.deleteAll().block();

    }

    @Test
    void findAllByStockQuantityGreaterThan() {
        Pageable pageable = PageRequest.of(0, 10);

        var productsInStock = productRepository.saveAll(List.of(product1, product2, product3))
                .thenMany(productRepository.findByStockQuantityGreaterThan(0, pageable))
                .toIterable();

        assertThat(productsInStock)
                .withFailMessage("В продаже есть какие-то товары")
                .isNotEmpty()
                .withFailMessage("Их 2")
                .hasSize(2)
                .first()
                .withFailMessage("Первый продукт: " + product1.getTitle())
                .extracting(Product::getTitle)
                .isEqualTo(product1.getTitle());
    }

    @Test
    void findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan_ShouldReturnMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(TITLE).ascending());
        String searchTerm = "Another";
        var matchingProducts = productRepository.saveAll(List.of(product1, product2, product3))
                .thenMany(productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(searchTerm, searchTerm, 0, pageable))
                .toIterable();

        assertThat(matchingProducts)
                .withFailMessage("В продаже есть какие-то товары")
                .isNotEmpty()
                .withFailMessage("Их 1")
                .hasSize(1)
                .last()
                .withFailMessage("Первый продукт: " + product2.getTitle())
                .extracting(Product::getTitle)
                .isEqualTo(product2.getTitle());
    }


    @Test
    void findImgPathById_ShouldReturnCorrectPath() {
        String actualImagePath = productRepository.save(product1)
                .map(Product::getId) // Получаем productId
                .flatMap(productRepository::findImgPathById)
                .block();

        assertNotNull(actualImagePath);
        assertThat(actualImagePath)
                .withFailMessage("Файл изображения должен быть")
                .isEqualTo(product1.getImgPath());
    }

}