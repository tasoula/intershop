package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.dto.ProductDto;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import io.github.tasoula.intershop.interceptor.CookieConstants;
import io.github.tasoula.intershop.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        String userIdStr = (String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME);
        UUID userId = (userIdStr==null || userIdStr.isEmpty()) ? null: UUID.fromString(userIdStr);

        Page<ProductDto> productPage = service.findAll(userId, search, pageable);
        model.addAttribute("paging", productPage);
        model.addAttribute("items", productPage.getContent());

        return "catalog.html";
    }

    @GetMapping("items/{id}")
    public String showItemById(HttpServletRequest request, @PathVariable("id") UUID id, Model model){
        UUID userId = UUID.fromString((String) request.getAttribute(CookieConstants.USER_ID_COOKIE_NAME));
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

        try {
            // 1. Сохранение изображения
            String imgPath = saveImage(image);
            if (imgPath == null) {
                model.addAttribute("errorMessage", "Ошибка сохранения изображения.");
                return "new-product"; // Возврат к форме с сообщением об ошибке
            }

            service.createProduct(title, description, image, price, stockQuantity);

            // 4. Перенаправление на страницу списка продуктов
            return "redirect:/catalog/items";

        } catch (IOException e) {
            model.addAttribute("errorMessage", "Ошибка при обработке изображения: " + e.getMessage());
            return "new-product"; // Возврат к форме с сообщением об ошибке
        }
    }


    private String saveImage(MultipartFile image) throws IOException {
   /*     if (image.isEmpty()) {
            return null;
        }

        // Путь к директории для сохранения изображений (должна существовать)
        String uploadDir = "src/main/resources/static/images"; // Относительный путь
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерируем уникальное имя файла
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Сохраняем файл
        Files.copy(image.getInputStream(), filePath);

        return "/images/" + fileName; // Возвращаем путь к файлу для сохранения в базе данных*/
        return "";
    }
}
