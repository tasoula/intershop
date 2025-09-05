package io.github.tasoula.intershop.dao;

import io.github.tasoula.intershop.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest extends SpringBootPostgreSQLBase{
    /*   @Autowired
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
        productRepository.save(product1);

        product2 = new Product();
        product2.setTitle("Another Product");
        product2.setDescription("Another great product.");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStockQuantity(5);
        product2.setImgPath("/images/product2.jpg");
        productRepository.save(product2);

        product3 = new Product();
        product3.setTitle("Low Stock Product");
        product3.setDescription("This product has low stock.");
        product3.setPrice(new BigDecimal("10.00"));
        product3.setStockQuantity(0);
        product3.setImgPath("/images/product3.jpg");
        productRepository.save(product3);
    }

    @Test
    void findImgPathById_ShouldReturnCorrectPath() {
        String imgPath = productRepository.findImgPathById(product1.getId());
        assertThat(imgPath).isEqualTo("/images/product1.jpg");
    }

    @Test
    void findAllByStockQuantityGreaterThan_ShouldReturnProductsWithSufficientStock() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = productRepository.findAllByStockQuantityGreaterThan(0, pageable);
        assertThat(products.getTotalElements()).isEqualTo(2); // product1 and product2
        assertThat(products.getContent()).contains(product1, product2);
        assertThat(products.getContent()).doesNotContain(product3);
    }

    @Test
    void findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan_ShouldReturnMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "product";
        Page<Product> products = productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm, searchTerm, 0, pageable);

        assertThat(products.getTotalElements()).isEqualTo(2); // product1 and product2
        assertThat(products.getContent()).contains(product1, product2);
        assertThat(products.getContent()).doesNotContain(product3);

        searchTerm = "amazing";
        products = productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm, searchTerm, 0, pageable);

        assertThat(products.getTotalElements()).isEqualTo(1); //product1
        assertThat(products.getContent()).contains(product1);
        assertThat(products.getContent()).doesNotContain(product2, product3);

        searchTerm = "low stock";
        products = productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(
                searchTerm, searchTerm, 0, pageable);

        assertThat(products.getTotalElements()).isEqualTo(0); //None because stock quantity is 0.
    }

     */
}