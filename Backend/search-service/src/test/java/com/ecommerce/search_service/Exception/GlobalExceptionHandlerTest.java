package com.ecommerce.search_service.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("should return 400 for InvalidSearchRequestException")
    void handleInvalidSearchRequest() {
        InvalidSearchRequestException ex = new InvalidSearchRequestException("Invalid page size");

        StepVerifier.create(handler.handleInvalidSearchRequest(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("status")).isEqualTo(400);
                    assertThat(body.get("message")).isEqualTo("Invalid page size");
                    assertThat(body.get("error")).isEqualTo("Bad Request");
                    assertThat(body.get("timestamp")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 500 for IndexingException")
    void handleIndexingException() {
        IndexingException ex = new IndexingException("ES connection failed");

        StepVerifier.create(handler.handleIndexingException(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("success")).isEqualTo(false);
                    assertThat(body.get("status")).isEqualTo(500);
                    assertThat(body.get("message")).isEqualTo("Indexing operation failed");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 500 for SearchException")
    void handleSearchException() {
        SearchException ex = new SearchException("Search query timeout");

        StepVerifier.create(handler.handleSearchException(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("message")).isEqualTo("Search operation failed");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 400 for IllegalArgumentException")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad argument");

        StepVerifier.create(handler.handleIllegalArgument(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("message")).isEqualTo("Bad argument");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 500 for generic Exception without exposing internals")
    void handleGenericException() {
        Exception ex = new RuntimeException("Internal database error with credentials");

        StepVerifier.create(handler.handleGenericException(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("message")).isEqualTo("An unexpected error occurred");
                    assertThat(body.get("message").toString()).doesNotContain("credentials");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should include all required fields in error response")
    void errorResponseStructure() {
        InvalidSearchRequestException ex = new InvalidSearchRequestException("test");

        StepVerifier.create(handler.handleInvalidSearchRequest(ex))
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKeys("success", "status", "error", "message", "timestamp");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should not expose internal details for IndexingException")
    void indexingExceptionHidesInternals() {
        IndexingException ex = new IndexingException("Connection refused to ES at 10.0.0.1:9200");

        StepVerifier.create(handler.handleIndexingException(ex))
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body.get("message")).isEqualTo("Indexing operation failed");
                    assertThat(body.get("message").toString()).doesNotContain("10.0.0.1");
                })
                .verifyComplete();
    }
}
