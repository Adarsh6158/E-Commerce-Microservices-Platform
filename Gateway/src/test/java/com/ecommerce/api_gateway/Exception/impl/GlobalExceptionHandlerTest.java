package com.ecommerce.api_gateway.Exception.impl;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import com.ecommerce.api_gateway.Exception.interfaces.IErrorResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private IErrorResponseHandler errorResponseHandler;

    @Mock
    private IGatewayLogger logger;

    private GlobalExceptionHandler exceptionHandler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(errorResponseHandler, logger);
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("correlationId", "corr-123");
    }

    @Test
    @DisplayName("Should handle ResponseStatusException and return correct status")
    void handle_whenResponseStatusException_shouldReturnMappedStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @Test
    @DisplayName("Should handle ConnectException and return 503")
    void handle_whenConnectException_shouldReturn503() {
        ConnectException ex = new ConnectException("Connection refused");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");
    }

    @Test
    @DisplayName("Should handle TimeoutException and return 504")
    void handle_whenTimeoutException_shouldReturn504() {
        TimeoutException ex = new TimeoutException("Timed out");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.GATEWAY_TIMEOUT, "Service request timed out");
    }

    @Test
    @DisplayName("Should handle generic exception and return 500")
    void handle_whenGenericException_shouldReturn500() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    @DisplayName("Should log error for unexpected exceptions")
    void handle_shouldLogError() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(logger).error(eq(GlobalExceptionHandler.class), anyString(), eq("/test"), eq("corr-123"), eq(ex));
    }
    
    @Test
    @DisplayName("Should handle missing correlation ID gracefully")
    void handle_whenNoCorrelationId_shouldLogNull() {
        exchange.getAttributes().remove("correlationId");
        RuntimeException ex = new RuntimeException("Oops");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(logger).error(eq(GlobalExceptionHandler.class), anyString(), eq("/test"), eq((Object)null), eq(ex));
    }

    @Test
    @DisplayName("Should return empty Mono when underlying handler fails")
    void handle_whenResponseHandlerFails_shouldReturnEmpty() {
        RuntimeException ex = new RuntimeException("Oops");
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Secondary failure")));
        reactor.test.StepVerifier.create(exceptionHandler.handle(exchange, ex)).expectError().verify();
    }

    @Test
    @DisplayName("Should capture nested exceptions in ResponseStatusException")
    void handle_whenNestedResponseStatusException_shouldLogProperly() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden", new RuntimeException("Inner"));
        when(errorResponseHandler.sendErrorResponse(any(), any(), any())).thenReturn(Mono.empty());
        exceptionHandler.handle(exchange, ex).block();
        verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.FORBIDDEN, "Forbidden");
    }
}
