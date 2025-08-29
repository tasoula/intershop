package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public Optional<Resource> getImage(UUID postId) {
        String fileName = repository.findImgPathById(postId);
        if (fileName == null) {
            return Optional.empty();
        }

        Path filePath = Paths.get(uploadDir, fileName);
        return Optional.of(new FileSystemResource(filePath.toFile()));
    }

    public void saveToDisc(MultipartFile file, String imagePath) {
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
}
