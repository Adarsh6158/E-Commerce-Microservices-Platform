package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Config.CatalogProperties;
import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import com.ecommerce.catalog_service.Service.Impl.ImageServiceImpl;
import com.ecommerce.catalog_service.Validator.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceImplTest {

    @TempDir
    Path tempDir;

    private ImageServiceImpl service;

    @BeforeEach
    void setUp() {
        CatalogProperties properties = new CatalogProperties();
        properties.getImages().setUploadDir(tempDir.toString());
        properties.getImages().setBaseUrl("http://localhost:8082/products/images");

        ImageValidator imageValidator = new ImageValidator();

        service = new ImageServiceImpl(properties, null, imageValidator);
    }

    @Test
    void resolveImage_validFilename_returnsPath() {
        Path resolved = service.resolveImage("image.jpg");
        assertNotNull(resolved);
        assertTrue(resolved.toString().endsWith("image.jpg"));
    }

    @Test
    void resolveImage_pathTraversal_throws() {
        assertThrows(InvalidRequestException.class,
                () -> service.resolveImage("../../etc/passwd"));
    }

    @Test
    void resolveImage_nestedTraversal_throws() {
        assertThrows(InvalidRequestException.class,
                () -> service.resolveImage("../../../etc/shadow"));
    }
}
