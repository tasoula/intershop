package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.annotations.UserId;
import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    public String showItems(@UserId UUID userId,
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
        Page<ProductDto> productPage = service.findAll(userId, search, pageable);
        model.addAttribute("paging", productPage);
        model.addAttribute("items", productPage.getContent());

        return "catalog.html";
    }

    @GetMapping("items/{id}")
    public String showItemById(@UserId UUID userId, @PathVariable("id") UUID id, Model model) {
        model.addAttribute("item", service.findById(userId, id));
        return "item.html";
    }


    @GetMapping("/products/new") // URL для отображения формы
    public String newProductForm(Model model) {
        // Можно добавить атрибуты в модель, если они необходимы (например, для ошибок)
        return "new-product"; // Имя вашего Thymeleaf шаблона
    }

    @PostMapping("/products")
    public String createProduct(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") int stockQuantity,
            Model model) {

        service.createProduct(title, description, image, price, stockQuantity);
        return "redirect:/catalog/items";
    }
}