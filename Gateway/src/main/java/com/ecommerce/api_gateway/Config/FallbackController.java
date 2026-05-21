package com.ecommerce.api_gateway.Config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;

@RestController
@RequestMapping("/fallback")
@Tag(name = "Fallback Controller", description = "Endpoints providing graceful 503 Service Unavailable responses when microservices are down or circuit breakers trigger")
public class FallbackController {

    private final IGatewayLogger logger;

    public FallbackController(IGatewayLogger logger) {
        this.logger = logger;
    }

    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Auth Service fallback", description = "Triggered when the Auth Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Auth Service is temporarily unavailable")
    public Mono<Map<String, Object>> authFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Auth Service");
    }

    @GetMapping(value = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Catalog Service fallback", description = "Triggered when the Catalog Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Catalog Service is temporarily unavailable")
    public Mono<Map<String, Object>> catalogFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Catalog Service");
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search Service fallback", description = "Triggered when the Search Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Search Service is temporarily unavailable")
    public Mono<Map<String, Object>> searchFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Search Service");
    }

    @GetMapping(value = "/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cart Service fallback", description = "Triggered when the Cart Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Cart Service is temporarily unavailable")
    public Mono<Map<String, Object>> cartFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Cart Service");
    }

    @GetMapping(value = "/pricing", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Pricing Service fallback", description = "Triggered when the Pricing Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Pricing Service is temporarily unavailable")
    public Mono<Map<String, Object>> pricingFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Pricing Service");
    }

    @GetMapping(value = "/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Inventory Service fallback", description = "Triggered when the Inventory Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Inventory Service is temporarily unavailable")
    public Mono<Map<String, Object>> inventoryFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Inventory Service");
    }

    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Order Service fallback", description = "Triggered when the Order Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Order Service is temporarily unavailable")
    public Mono<Map<String, Object>> orderFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Order Service");
    }

    @GetMapping(value = "/payment", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Payment Service fallback", description = "Triggered when the Payment Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Payment Service is temporarily unavailable")
    public Mono<Map<String, Object>> paymentFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Payment Service");
    }

    @GetMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Service fallback", description = "Triggered when the Notification Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Notification Service is temporarily unavailable")
    public Mono<Map<String, Object>> notificationFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Notification Service");
    }

    @GetMapping(value = "/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Analytics Service fallback", description = "Triggered when the Analytics Service is unhealthy, times out, or when its circuit breaker is in the open state.")
    @ApiResponse(responseCode = "503", description = "Analytics Service is temporarily unavailable")
    public Mono<Map<String, Object>> analyticsFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Analytics Service");
    }

    private Mono<Map<String, Object>> buildFallbackResponse(ServerWebExchange exchange, String serviceName) {
        String correlationId = exchange.getAttribute("correlationId");

        logger.warn(FallbackController.class, "Circuit breaker fallback triggered. service={}, correlationId={}", serviceName, correlationId);

        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 503);
        body.put("error", "Service Unavailable");
        body.put("message", serviceName + " is temporarily unavailable. Please retry later.");
        body.put("correlationId", correlationId);

        return Mono.just(body);
    }
}