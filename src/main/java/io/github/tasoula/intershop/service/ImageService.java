package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    @Value("${upload.images.dir}")
    private String uploadDir;

    private final ProductRepository repository;

    public ImageService(ProductRepository repository) {
        this.repository = repository;
    }

    public Mono<Resource> getImage(UUID postId) {
        return repository.findImgPathById(postId)
                .filter(Objects::nonNull) // Отфильтровываем null значения
                .map(fileName -> Paths.get(uploadDir, fileName)) // Формируем Path
                .map(filePath -> (Resource) new FileSystemResource(filePath.toFile())) // Создаем Resource
                .onErrorResume(e -> {
                    log.error("Ошибка при получении изображения для ID {}: {}", postId, e.getMessage(), e);
                    return Mono.empty(); // Возвращаем пустой Mono в случае ошибки
                });
    }

 /*   public void saveToDisc(MultipartFile file, String imagePath) {
        if (uploadDir == null) {
            throw new IllegalStateException("Требуется задать каталог для сохранения изображений поста");
        }
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. Сохранить файл на диск
        Path filePath = Paths.get(uploadDir, imagePath);
        try {
            Files.copy(file.getInputStream(), filePath);
        }
        catch (IOException e){
            log.error("Не удалось загрузить изобраение: {}", e.getMessage(), e);
        }
    }
*/
    public Mono<Void> saveToDisc(FilePart filePart, String imagePath) { // Используем FilePart для WebFlux
        if (uploadDir == null) {
            return Mono.error(new IllegalStateException("Требуется задать каталог для сохранения изображений поста"));
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return Mono.error(new IllegalStateException("Не удалось создать каталог для сохранения изображений"));
            }
        }

        Path filePath = Paths.get(uploadDir, imagePath);

        return filePart.transferTo(filePath) // Используем transferTo для асинхронной записи
                .doOnError(e -> log.error("Не удалось загрузить изображение: {}", e.getMessage(), e))
                .then(); // Возвращаем Mono<Void> для асинхронности
    }
}

