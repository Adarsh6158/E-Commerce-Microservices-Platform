package com.ecommerce.api_gateway.Exception.impl;

import com.ecommerce.api_gateway.Exception.interfaces.IErrorResponseHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.ecommerce.api_gateway.constants.GatewayConstants;
import com.ecommerce.api_gateway.dto.response.ApiErrorResponse;

import java.nio.charset.StandardCharsets;

@Component
public class JsonErrorResponseHandler implements IErrorResponseHandler {

    private final ObjectMapper objectMapper;

    public JsonErrorResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> sendErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.empty();
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String correlationId = exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR);
        String path = exchange.getRequest().getURI().getPath();

        ApiErrorResponse errorBody = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                correlationId
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            String fallback = String.format("{\"error\":\"Internal Server Error\",\"message\":\"%s\",\"correlationId\":\"%s\"}", message, correlationId);
            DataBuffer buffer = response.bufferFactory().wrap(fallback.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}
