package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import com.ecommerce.api_gateway.constants.GatewayConstants;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
@Tag(name = "Request Logging Filter", description = "Global Gateway Filter logging inbound requests and downstream execution latency & HTTP status codes")
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private final IGatewayLogger logger;
    private final MeterRegistry meterRegistry;

    public RequestLoggingFilter(IGatewayLogger logger, MeterRegistry meterRegistry) {
        this.logger = logger;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Operation(summary = "Log HTTP exchange telemetry", description = "Logs entry trace containing HTTP method, path, query parameters, client IP, user ID, and correlation ID. On reactive chain completion, captures the response status code and execution latency, emitting detailed audit logs categorized by HTTP response status level (INFO, WARN, ERROR).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "HTTP request logged and lifecycle trace finalized successfully")
    })
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR);
        String userId = exchange.getAttribute(GatewayConstants.USER_ID_ATTR);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();

        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        logger.info(RequestLoggingFilter.class, ">>> GATEWAY REQUEST: method={}, path={}, query={}, clientIp={}, userId={}, correlationId={}",
                method, path, query, clientIp, userId, correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> {

                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();

                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;

                    org.springframework.cloud.gateway.route.Route route = exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "unknown_route";
                    
                    meterRegistry.timer("gateway.requests",
                            "route_id", routeId,
                            "http_status", String.valueOf(statusCode),
                            "downstream_service", routeId)
                            .record(duration, TimeUnit.MILLISECONDS);

                    if (statusCode >= 500) {
                        logger.error(RequestLoggingFilter.class, "<<< GATEWAY RESPONSE: method={}, path={}, status={}, duration={}ms, correlationId={}",
                                method, path, statusCode, duration, correlationId);
                    } else if (statusCode >= 400) {
                        logger.warn(RequestLoggingFilter.class, "<<< GATEWAY RESPONSE: method={}, path={}, status={}, duration={}ms, correlationId={}",
                                method, path, statusCode, duration, correlationId);
                    } else {
                        logger.info(RequestLoggingFilter.class, "<<< GATEWAY RESPONSE: method={}, path={}, status={}, duration={}ms, correlationId={}",
                                method, path, statusCode, duration, correlationId);
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}