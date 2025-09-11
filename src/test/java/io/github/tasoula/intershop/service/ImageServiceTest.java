package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
    @Mock
    private ProductRepository repository;
    @InjectMocks
    private ImageService imageService;
    @Value("${upload.images.dir}")
    private String uploadDir = "test-upload";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "uploadDir", uploadDir);

        // Clean up the directory before each test
        File dir = new File(uploadDir);
        if (dir.exists()) {
            deleteDirectory(dir);
        }
    }

   private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    void getImage_existingImage_returnsResource() throws IOException {
        UUID postId = UUID.randomUUID();
        String fileName = "image.jpg";
        when(repository.findImgPathById(postId)).thenReturn(Mono.just(fileName));

        // Create a dummy file in the upload directory
        Path filePath = Paths.get(uploadDir, fileName);
        File file = filePath.toFile();
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            fail("Failed to create dummy file: " + e.getMessage());
        }

        Resource resource = imageService.getImage(postId).block();
        assertNotNull(resource);
        assertInstanceOf(FileSystemResource.class, resource);

        // Replace backslashes with forward slashes
        String expectedPath = filePath.toString().replace("\\", "/");
        assertEquals(expectedPath,  ((FileSystemResource) resource).getPath());

        // Clean up the dummy file
        file.delete();
    }

     @Test
    void getImage_nonExistingImage_returnsEmptyOptional() {
        UUID postId = UUID.randomUUID();
        when(repository.findImgPathById(postId)).thenReturn(Mono.empty());

        Resource resource = imageService.getImage(postId).block();

        assertNull(resource);
    }

    @Test
    void saveToDisc_validFile_savesFileToDisk() {
        FilePart filePart = mock(FilePart.class);
        String imagePath = "test_image.jpg";
        Path fullPath = Paths.get(uploadDir, imagePath);

        when(filePart.transferTo(any(Path.class))).thenReturn(Mono.empty());

        Mono<Void> saveMono = imageService.saveToDisc(filePart, imagePath);

        StepVerifier.create(saveMono)
                .verifyComplete();

        verify(filePart, times(1)).transferTo(fullPath);

        // Clean up the test file
        try {
            Files.deleteIfExists(fullPath);
            Files.deleteIfExists(Paths.get(uploadDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @Test
    void saveToDisc_noUploadDir_throwsException() {
        // Создаем новый экземпляр imageService с null uploadDir
        ReflectionTestUtils.setField(imageService, "uploadDir", null);
        FilePart filePart = mock(FilePart.class);
        String imagePath = "test_image.jpg";

        Mono<Void> saveMono = imageService.saveToDisc(filePart, imagePath);

        StepVerifier.create(saveMono)
                .expectError(IllegalStateException.class)
                .verify();
    }
}