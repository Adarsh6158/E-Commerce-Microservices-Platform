package com.ecommerce.order_service.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleInvalidOrderRequest_shouldReturn400() {
        InvalidOrderRequestException ex = new InvalidOrderRequestException("Invalid items");

        StepVerifier.create(handler.handleInvalidOrderRequest(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).containsEntry("success", false);
                    assertThat(response.getBody()).containsEntry("status", 400);
                    assertThat(response.getBody()).containsEntry("message", "Invalid items");
                })
                .verifyComplete();
    }

    @Test
    void handleOrderNotFound_shouldReturn404() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found: abc");

        StepVerifier.create(handler.handleOrderNotFound(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(response.getBody()).containsEntry("status", 404);
                    assertThat(response.getBody()).containsEntry("message", "Order not found: abc");
                })
                .verifyComplete();
    }

    @Test
    void handleOrderCancellation_shouldReturn409() {
        OrderCancellationException ex = new OrderCancellationException("Cannot cancel confirmed order");

        StepVerifier.create(handler.handleOrderCancellation(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(response.getBody()).containsEntry("status", 409);
                })
                .verifyComplete();
    }

    @Test
    void handleOrderServiceException_shouldReturn500() {
        OrderServiceException ex = new OrderServiceException("Internal error");

        StepVerifier.create(handler.handleOrderServiceException(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).containsEntry("message", "An order service error occurred");
                })
                .verifyComplete();
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("bad param");

        StepVerifier.create(handler.handleIllegalArgument(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).containsEntry("message", "bad param");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_shouldReturn500WithGenericMessage() {
        Exception ex = new RuntimeException("something broke");

        StepVerifier.create(handler.handleGenericException(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
                    assertThat(response.getBody()).doesNotContainValue("something broke");
                })
                .verifyComplete();
    }

    @Test
    void errorResponse_shouldContainTimestamp() {
        InvalidOrderRequestException ex = new InvalidOrderRequestException("test");

        StepVerifier.create(handler.handleInvalidOrderRequest(ex))
                .assertNext(response -> assertThat(response.getBody()).containsKey("timestamp"))
                .verifyComplete();
    }

    @Test
    void errorResponse_shouldContainErrorField() {
        OrderNotFoundException ex = new OrderNotFoundException("not found");

        StepVerifier.create(handler.handleOrderNotFound(ex))
                .assertNext(response -> assertThat(response.getBody()).containsEntry("error", "Not Found"))
                .verifyComplete();
    }
}
