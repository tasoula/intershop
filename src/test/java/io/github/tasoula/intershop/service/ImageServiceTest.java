package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
 /*   @Mock
    private ProductRepository repository;

    @InjectMocks
    private ImageService imageService;

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
    void getImage_existingImage_returnsResource() {
        UUID postId = UUID.randomUUID();
        String fileName = "image.jpg";
        when(repository.findImgPathById(postId)).thenReturn(fileName);

        // Create a dummy file in the upload directory
        Path filePath = Paths.get(uploadDir, fileName);
        File file = filePath.toFile();
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            fail("Failed to create dummy file: " + e.getMessage());
        }

        Optional<Resource> resource = imageService.getImage(postId);
        assertTrue(resource.isPresent());
        assertInstanceOf(FileSystemResource.class, resource.get());

        // Replace backslashes with forward slashes
        String expectedPath = filePath.toString().replace("\\", "/");
        assertEquals(expectedPath, ((FileSystemResource) resource.get()).getPath());

        // Clean up the dummy file
        file.delete();
    }

    @Test
    void getImage_nonExistingImage_returnsEmptyOptional() {
        UUID postId = UUID.randomUUID();
        when(repository.findImgPathById(postId)).thenReturn(null);

        Optional<Resource> resource = imageService.getImage(postId);

        assertFalse(resource.isPresent());
    }

    @Test
    void saveToDisc_validFile_savesFileToDisk() throws IOException {
        String imagePath = "test.jpg";
        String content = "test image content";
        MultipartFile file = new MockMultipartFile("file", imagePath, "image/jpeg", content.getBytes());

        imageService.saveToDisc(file, imagePath);

        Path filePath = Paths.get(uploadDir, imagePath);
        File savedFile = filePath.toFile();
        assertTrue(savedFile.exists());

        // Verify the content of the saved file
        String savedContent = new String(Files.readAllBytes(filePath));
        assertEquals(content, savedContent);

        //Clean up file after verification
        Files.delete(filePath);
    }

    @Test
    void saveToDisc_IOException_logsError() throws IOException {
        String imagePath = "test.jpg";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Simulated IO exception"));

        imageService.saveToDisc(file, imagePath);

        // This test primarily checks for error logging.  You'd typically verify log output here.
        // Since we're not directly verifying log output, we at least ensure that the exception
        // doesn't propagate and that the method completes without crashing.  A more robust
        // implementation would involve a logging framework and assertions against the logged messages.
        Path filePath = Paths.get(uploadDir, imagePath);
        File savedFile = filePath.toFile();
        assertFalse(savedFile.exists());

    }

    @Test
    void saveToDisc_nullUploadDir_throwsIllegalStateException() {
        ReflectionTestUtils.setField(imageService, "uploadDir", null);
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

        assertThrows(IllegalStateException.class, () -> imageService.saveToDisc(file, "test.jpg"));
    }

  */
}