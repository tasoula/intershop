package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }


    public Page<Product> findAll(String search, Pageable pageable) {
        if (search == null || search.isEmpty())
            return repository.findAll(pageable);

        return repository.findByTitleContainingOrDescriptionContainingIgnoreCase(search.toLowerCase(), pageable);
    }
}
