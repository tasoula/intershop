package io.github.tasoula.intershop.controller;

import io.github.tasoula.intershop.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/catalog")
public class ImageController {

    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    @GetMapping("images/{id}")
    public Mono<ResponseEntity<?>> image(@PathVariable("id") UUID id) {
        return service.getImage(id) // Возвращаем Mono<Resource> из service.getImage()
                .flatMap(resource -> {
                    if (resource == null || !resource.exists()) { // Проверяем на null и существование
                        return Mono.just(ResponseEntity.notFound().build()); // 404
                    }

                    try {
                        return Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG) // Установите правильный Content-Type
                                .body(resource));
                    } catch (Exception e) {
                        log.error("Произошла ошибка при обработке изображения: {}", e.getMessage(), e);
                        return Mono.just(ResponseEntity.internalServerError().build()); // 500
                    }
                })
                .onErrorResume(e -> { // Обработка общих ошибок, таких как исключения при работе с service
                    log.error("Произошла ошибка при получении изображения: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}