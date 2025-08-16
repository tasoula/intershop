package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/catalog")
public class ImageController {

    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    @GetMapping("images/{id}")
    public ResponseEntity<Resource> image(@PathVariable("id") UUID id) {
        Optional<Resource> resource = service.getImage(id);
        if (resource.isEmpty() || !resource.get().exists()) {
            return ResponseEntity.notFound().build(); // Если файл не существует, возвращаем 404
        }

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Установите правильный Content-Type в зависимости от типа изображения
                    .body(resource.get());
        } catch (Exception e) {
            log.error("Произошла ошибка: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build(); // Обработка ошибок при чтении файла
        }
    }
}