package com.ecommerce.api_gateway.Exception.impl;

import com.ecommerce.api_gateway.Exception.interfaces.IErrorResponseHandler;
import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import com.ecommerce.api_gateway.constants.GatewayConstants;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final IErrorResponseHandler errorResponseHandler;
    private final IGatewayLogger logger;

    public GlobalExceptionHandler(IErrorResponseHandler errorResponseHandler, IGatewayLogger logger) {
        this.errorResponseHandler = errorResponseHandler;
        this.logger = logger;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        String correlationId = exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR);
        String path = exchange.getRequest().getURI().getPath();

        HttpStatus status;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else if (ex instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable";
            logger.error(GlobalExceptionHandler.class, "Downstream service connection failed. path={}, correlationId={}", path, correlationId, ex);
        } else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Service request timed out";
            logger.error(GlobalExceptionHandler.class, "Downstream service timed out. path={}, correlationId={}", path, correlationId, ex);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred";
            logger.error(GlobalExceptionHandler.class, "Unhandled gateway error. path={}, correlationId={}", path, correlationId, ex);
        }

        return errorResponseHandler.sendErrorResponse(exchange, status, message);
    }
}