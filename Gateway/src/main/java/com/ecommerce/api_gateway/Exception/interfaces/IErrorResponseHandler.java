package com.ecommerce.api_gateway.Exception.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface IErrorResponseHandler {
    Mono<Void> sendErrorResponse(ServerWebExchange exchange, HttpStatus status, String message);
}
