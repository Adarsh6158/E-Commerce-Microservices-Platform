package com.ecommerce.api_gateway.Exception.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonErrorResponseHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    private JsonErrorResponseHandler handler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new JsonErrorResponseHandler(objectMapper);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("correlationId", "test-corr-123");
    }

    @Test
    @DisplayName("Should successfully send JSON error response")
    void sendErrorResponse_whenValid_shouldWriteResponse() throws Exception {
        String jsonResult = "{\"error\":\"Not Found\"}";
        when(objectMapper.writeValueAsBytes(any())).thenReturn(jsonResult.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.NOT_FOUND, "Resource not found"))
                .verifyComplete();

        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(
                response.getHeaders().getContentType().includes(org.springframework.http.MediaType.APPLICATION_JSON));
        assertEquals(jsonResult, response.getBodyAsString().block());
    }

    @Test
    @DisplayName("Should fallback to manual JSON when ObjectMapper throws JsonProcessingException")
    void sendErrorResponse_whenJsonProcessingException_shouldFallback() throws Exception {
        when(objectMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("Mock exception") {
        });

        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Bad error"))
                .verifyComplete();

        MockServerHttpResponse response = exchange.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String body = response.getBodyAsString().block();
        assertTrue(body.contains("\"error\":\"Internal Server Error\""));
    }

    @Test
    @DisplayName("Should return empty when response is already committed")
    void sendErrorResponse_whenResponseCommitted_shouldReturnEmpty() {
        exchange.getResponse().setComplete().block();
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle missing correlationId gracefully")
    void sendErrorResponse_whenNoCorrelationId_shouldHandle() throws Exception {
        exchange.getAttributes().remove("correlationId");
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Err")).verifyComplete();
    }

    @Test
    @DisplayName("Should handle 401 Unauthorized properly")
    void sendErrorResponse_when401_shouldSetStatus() throws Exception {
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Err")).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Should handle 403 Forbidden properly")
    void sendErrorResponse_when403_shouldSetStatus() throws Exception {
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.FORBIDDEN, "Err")).verifyComplete();
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Should handle 429 Too Many Requests properly")
    void sendErrorResponse_when429_shouldSetStatus() throws Exception {
        when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.TOO_MANY_REQUESTS, "Err")).verifyComplete();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Should fallback gracefully when message contains quotes")
    void sendErrorResponse_whenMessageHasQuotes_shouldEscapeInFallback() throws Exception {
        when(objectMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("Mock") {
        });
        StepVerifier.create(handler.sendErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Message with \"quotes\""))
                .verifyComplete();
        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("Message with \"quotes\""));
    }
}
