package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    /*   private final CartService cartService;

    private final ImageService imageService;


     */

    public ProductService(ProductRepository repository) {
        this.productRepository = repository;
    }

    public Mono<Page<ProductDto>> findTest(UUID userId, String search, Pageable pageable) {
        Product product1 = new Product(UUID.randomUUID(), "title 1", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product2 = new Product(UUID.randomUUID(), "title 2", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product3 = new Product(UUID.randomUUID(), "title 3", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product4 = new Product(UUID.randomUUID(), "title 4", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product5 = new Product(UUID.randomUUID(), "title 5", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product6 = new Product(UUID.randomUUID(), "title 6", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product7 = new Product(UUID.randomUUID(), "title 7", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product8 = new Product(UUID.randomUUID(), "title 8", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product9 = new Product(UUID.randomUUID(), "title 9", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product10 = new Product(UUID.randomUUID(), "title 10", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product11 = new Product(UUID.randomUUID(), "title 11", "description 1", "6062209614.webp", BigDecimal.TEN, 8);

        Flux<ProductDto> productDtoFlux = Flux.just(product1, product2, product3, product4, product5, product6, product7, product8, product9, product10,product11) // productRepository.findByStockQuantityGreaterThan(0, pageable)
                .flatMap(product -> mapToDto(userId, product));

        Mono<Long> totalMono = Mono.just(11L); //productRepository.count(); // Получаем общее количество элементов

        Mono<List<ProductDto>> productDtoListMono = productDtoFlux.collectList();

        Mono<Page<ProductDto>> result = Mono.zip(productDtoListMono, totalMono)
                .map(tuple -> {
                    List<ProductDto> productDtoList = tuple.getT1();
                    long total = tuple.getT2();
                    return new PageImpl<>(productDtoList, pageable, total);
                });

        return result;
    }

    public Mono<Page<ProductDto>> findAll(UUID userId, String search, Pageable pageable) {
     //   Mono<Page<Product>> productPageMono = (search == null || search.isEmpty())
     //           ? productRepository.findAllByStockQuantityGreaterThan(0, pageable) // Поиск без фильтра
     //           : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(search.toLowerCase(), search.toLowerCase(), 0, pageable); // Поиск с фильтром

        Product product1 = new Product(UUID.randomUUID(), "title 1", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product2 = new Product(UUID.randomUUID(), "title 2", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product3 = new Product(UUID.randomUUID(), "title 3", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product4 = new Product(UUID.randomUUID(), "title 4", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product5 = new Product(UUID.randomUUID(), "title 5", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product6 = new Product(UUID.randomUUID(), "title 6", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product7 = new Product(UUID.randomUUID(), "title 7", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product8 = new Product(UUID.randomUUID(), "title 8", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product9 = new Product(UUID.randomUUID(), "title 9", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product10 = new Product(UUID.randomUUID(), "title 10", "description 1", "6062209614.webp", BigDecimal.TEN, 8);
        Product product11 = new Product(UUID.randomUUID(), "title 11", "description 1", "6062209614.webp", BigDecimal.TEN, 8);

        Flux<ProductDto> productDtoFlux = Flux.just(product1, product2, product3, product4, product5, product6, product7, product8, product9, product10,product11) // productRepository.findByStockQuantityGreaterThan(0, pageable)
                .flatMap(product -> mapToDto(userId, product));

        Mono<Long> totalMono = Mono.just(11L); //productRepository.count(); // Получаем общее количество элементов

        Mono<List<ProductDto>> productDtoListMono = productDtoFlux.collectList();

        Mono<Page<ProductDto>> result = Mono.zip(productDtoListMono, totalMono)
                .map(tuple -> {
                    List<ProductDto> productDtoList = tuple.getT1();
                    long total = tuple.getT2();
                    return new PageImpl<>(productDtoList, pageable, total);
                });

        return result;
    }

    private Mono<ProductDto> mapToDto(UUID userId, Product product) {
        // Асинхронно получаем количество товара в корзине.  cartService.getCartQuantity возвращает Mono<Integer>
        return Mono.just(0) // todo cartService.getCartQuantity(userId, product.getId())
                .map(cartQuantity -> new ProductDto(product, cartQuantity)); // Преобразуем в ProductDto
    }

/*
    public Page<ProductDto> findAll(UUID userId, String search, Pageable pageable) {
       Page<Product> productPage = (search == null || search.isEmpty())
                ? productRepository.findAllByStockQuantityGreaterThan(0, pageable)
                : productRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseAndStockQuantityGreaterThan(search.toLowerCase(), search.toLowerCase(), 0, pageable);

        List<ProductDto> productCatalogItemDtos = productPage.getContent().stream()
                .map(product -> mapToDto(userId, product))
                .collect(Collectors.toList());

        return new PageImpl<>(productCatalogItemDtos, pageable, productPage.getTotalElements());
    }



    public ProductDto findById(UUID userId, UUID productId) throws ResourceNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDto(userId, product);
    }

    private ProductDto mapToDto(UUID userId, Product product) {
        return new ProductDto(product, cartService.getCartQuantity(userId, product.getId()));
    }


    @Transactional
    public void createProduct(String title,
                              String description,
                              MultipartFile image,
                              BigDecimal price,
                              int stockQuantity) {

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);

        String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
        product.setImgPath(filename);

        productRepository.save(product);
        imageService.saveToDisc(image, filename);
    }

 */
}
