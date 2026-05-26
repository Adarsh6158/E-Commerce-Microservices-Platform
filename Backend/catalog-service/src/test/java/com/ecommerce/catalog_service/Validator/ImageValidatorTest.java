package com.ecommerce.catalog_service.Validator;

import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private ImageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImageValidator();
    }

    @Test
    void validateFilename_validFilename_noException() {
        assertDoesNotThrow(() -> validator.validateFilename("product_abc123.jpg"));
    }

    @Test
    void validateFilename_validWithDashes_noException() {
        assertDoesNotThrow(() -> validator.validateFilename("my-image.png"));
    }

    @Test
    void validateFilename_null_throws() {
        assertThrows(InvalidRequestException.class, () -> validator.validateFilename(null));
    }

    @Test
    void validateFilename_pathTraversal_throws() {
        assertThrows(InvalidRequestException.class, () -> validator.validateFilename("../etc/passwd"));
    }

    @Test
    void validateFilename_specialChars_throws() {
        assertThrows(InvalidRequestException.class, () -> validator.validateFilename("image<script>.jpg"));
    }

    @Test
    void validateFilename_noExtension_throws() {
        assertThrows(InvalidRequestException.class, () -> validator.validateFilename("imagefile"));
    }

    @Test
    void validateContentType_validType_noException() {
        Set<String> allowed = Set.of("image/jpeg", "image/png");
        assertDoesNotThrow(() -> validator.validateContentType("image/jpeg", allowed));
    }

    @Test
    void validateContentType_invalidType_throws() {
        Set<String> allowed = Set.of("image/jpeg", "image/png");
        assertThrows(InvalidRequestException.class,
                () -> validator.validateContentType("application/pdf", allowed));
    }

    @Test
    void validateContentType_null_throws() {
        Set<String> allowed = Set.of("image/jpeg");
        assertThrows(InvalidRequestException.class,
                () -> validator.validateContentType(null, allowed));
    }

    @Test
    void validateContentType_emptyString_throws() {
        Set<String> allowed = Set.of("image/jpeg");
        assertThrows(InvalidRequestException.class,
                () -> validator.validateContentType("", allowed));
    }
}
