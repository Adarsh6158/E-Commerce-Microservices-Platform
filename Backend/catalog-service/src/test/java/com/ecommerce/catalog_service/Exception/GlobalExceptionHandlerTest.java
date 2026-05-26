package com.ecommerce.catalog_service.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleProductNotFound_returns404() {
        ProductNotFoundException ex = new ProductNotFoundException("p1");
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertTrue(response.getBody().message().contains("p1"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleReviewNotFound_returns404() {
        ReviewNotFoundException ex = new ReviewNotFoundException("r1");
        ResponseEntity<ErrorResponse> response = handler.handleReviewNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
    }

    @Test
    void handleDuplicateReview_returns409() {
        DuplicateReviewException ex = new DuplicateReviewException("p1", "u1");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateReview(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
    }

    @Test
    void handleInvalidRequest_returns400() {
        InvalidRequestException ex = new InvalidRequestException("Bad input");
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad input", response.getBody().message());
    }

    @Test
    void handleImageStorage_returns500_withoutStackTrace() {
        ImageStorageException ex = new ImageStorageException("Disk full");
        ResponseEntity<ErrorResponse> response = handler.handleImageStorage(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());

        assertEquals("Image storage operation failed", response.getBody().message());
    }

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void handleIllegalState_returns409() {
        IllegalStateException ex = new IllegalStateException("bad state");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
    }

    @Test
    void handleGeneric_returns500_withoutLeakingDetails() {
        Exception ex = new RuntimeException("secret internal error");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertFalse(response.getBody().message().contains("secret"));
    }
}
