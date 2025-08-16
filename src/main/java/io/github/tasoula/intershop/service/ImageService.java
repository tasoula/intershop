package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

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
}
