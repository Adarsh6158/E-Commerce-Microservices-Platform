package com.ecommerce.catalog_service.Validator;

import com.ecommerce.catalog_service.Dto.Request.CreateProductRequest;
import com.ecommerce.catalog_service.Dto.Request.UpdateProductRequest;
import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {

    private ProductValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProductValidator();
    }

    @Test
    void validateCreateRequest_validRequest_noException() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-001", "Product", "Desc", "cat1", "Brand",
                new BigDecimal("19.99"), null, null, null, null, null, null
        );
        assertDoesNotThrow(() -> validator.validateCreateRequest(request));
    }

    @Test
    void validateCreateRequest_negativePrice_throws() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-001", "Product", "Desc", "cat1", "Brand",
                new BigDecimal("-5.00"), null, null, null, null, null, null
        );
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> validator.validateCreateRequest(request));
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    void validateCreateRequest_skuTooLong_throws() {
        String longSku = "A".repeat(51);
        CreateProductRequest request = new CreateProductRequest(
                longSku, "Product", "Desc", "cat1", "Brand",
                new BigDecimal("10.00"), null, null, null, null, null, null
        );
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> validator.validateCreateRequest(request));
        assertTrue(ex.getMessage().contains("SKU"));
    }

    @Test
    void validateCreateRequest_zeroPrice_noException() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-001", "Product", "Desc", "cat1", "Brand",
                BigDecimal.ZERO, null, null, null, null, null, null
        );
        assertDoesNotThrow(() -> validator.validateCreateRequest(request));
    }

    @Test
    void validateUpdateRequest_negativePrice_throws() {
        UpdateProductRequest request = new UpdateProductRequest(
                null, null, null, null, new BigDecimal("-1"),
                null, null, null, null, null, null, null
        );
        assertThrows(InvalidRequestException.class,
                () -> validator.validateUpdateRequest(request));
    }

    @Test
    void validateUpdateRequest_nullPrice_noException() {
        UpdateProductRequest request = new UpdateProductRequest(
                null, null, null, null, null,
                null, null, null, null, null, null, null
        );
        assertDoesNotThrow(() -> validator.validateUpdateRequest(request));
    }
}
