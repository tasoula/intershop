package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductCatalogItemDto;
import io.github.tasoula.intershop.interceptor.UserInterceptor;
import io.github.tasoula.intershop.service.ProductService;
import jakarta.servlet.http.Cookie;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/catalog")
public class ProductController {

    public static final String TITLE = "title";
    public static final String PRICE = "price";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping()
    public String show() {
        return "redirect:/catalog/items";
    }

    @GetMapping("items")
    public String showItems(HttpServletRequest request,
                            @RequestParam(name = "search", required = false) String search,
                            @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                            Model model) {

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        if (pageSize <= 0) pageSize = 10;
        if (pageNumber < 0) pageNumber = 0;

        Sort sortObj = switch (sort) {
            case "ALPHA" -> Sort.by(TITLE);
            case "PRICE" -> Sort.by(PRICE);
            default -> Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj.ascending());

      /*     List<Product> productItems = List.of(
                new Product(
                    UUID.randomUUID(),
                    "product",
                    "description",
                    "image.jpg",
                    BigDecimal.valueOf(100.00),
                        10)
        );

        Page<Product> productPage = new PageImpl<>(productItems, pageable, productItems.size());
    */

        String userIdStr = (String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME);
        UUID userId = (userIdStr==null || userIdStr.isEmpty()) ? null: UUID.fromString(userIdStr);

        Page<ProductCatalogItemDto> productPage = service.findAll(userId, search, pageable);
        model.addAttribute("paging", productPage);
        model.addAttribute("items", productPage.getContent());

        return "catalog.html";
    }

    @GetMapping("items/{id}")
    public String showItemById(HttpServletRequest request, @PathVariable("id") UUID id, Model model){
        UUID userId = UUID.fromString((String) request.getAttribute(UserInterceptor.USER_ID_COOKIE_NAME));
        model.addAttribute("item", service.findById(userId, id));
        return "item.html";
    }
}
