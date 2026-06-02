package com.ecommerce.order_service.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidOrderRequestException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidOrderRequest(InvalidOrderRequestException ex) {
        log.warn("Invalid order request: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(OrderCancellationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleOrderCancellation(OrderCancellationException ex) {
        log.warn("Order cancellation failed: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(OrderServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleOrderServiceException(OrderServiceException ex) {
        log.error("Order service error: {}", ex.getMessage(), ex);
        return Mono.just(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An order service error occurred"));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(WebExchangeBindException ex) {
        String message = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "success", false,
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(status).body(body);
    }
}
