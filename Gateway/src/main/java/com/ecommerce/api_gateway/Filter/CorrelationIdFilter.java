package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.Filter.interfaces.ICorrelationIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.ecommerce.api_gateway.constants.GatewayConstants;

@Component
@Tag(name = "Correlation ID Filter", description = "Global Gateway Filter managing distributed tracing correlation IDs for request tracking")
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private final ICorrelationIdGenerator correlationIdGenerator;

    public CorrelationIdFilter(ICorrelationIdGenerator correlationIdGenerator) {
        this.correlationIdGenerator = correlationIdGenerator;
    }

    @Override
    @Operation(summary = "Establish request correlation ID", description = "Checks incoming request headers for X-Correlation-Id. If missing, generates a new UUID. Then binds this tracking ID to the ServerWebExchange attributes, the Logback MDC thread context, the reactive contextual pipeline, and finally propagates it on the downstream mutated request and outbound response headers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correlation ID successfully generated or preserved, request/response headers mutated")
    })
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(GatewayConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = correlationIdGenerator.generate();
        }

        exchange.getAttributes().put(GatewayConstants.CORRELATION_ID_ATTR, correlationId);

        String finalCorrelationId = correlationId;
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(GatewayConstants.CORRELATION_ID_HEADER, correlationId)
                .build();

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(GatewayConstants.CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ctx -> ctx.put(GatewayConstants.CORRELATION_ID_ATTR, finalCorrelationId))
                .doFirst(() -> MDC.put(GatewayConstants.CORRELATION_ID_ATTR, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
