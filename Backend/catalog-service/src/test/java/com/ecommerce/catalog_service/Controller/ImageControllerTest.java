package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Service.ImageService;
import com.ecommerce.catalog_service.Validator.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock private ImageService imageService;

    private ImageValidator imageValidator;
    private ImageController controller;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageValidator = new ImageValidator();
        controller = new ImageController(imageService, imageValidator);
    }

    @Test
    void getImage_existingFile_returnsOk() throws IOException {
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, new byte[]{1, 2, 3});

        when(imageService.resolveImage("test.jpg")).thenReturn(testFile);

        StepVerifier.create(controller.getImage("test.jpg"))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void getImage_nonExistingFile_returnsNotFound() {
        Path nonExistent = tempDir.resolve("missing.jpg");
        when(imageService.resolveImage("missing.jpg")).thenReturn(nonExistent);

        StepVerifier.create(controller.getImage("missing.jpg"))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void getImage_invalidFilename_throwsValidationError() {

        org.junit.jupiter.api.Assertions.assertThrows(
                com.ecommerce.catalog_service.Exception.InvalidRequestException.class,
                () -> controller.getImage("../etc/passwd")
        );
    }
}
