package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import com.ecommerce.api_gateway.Exception.interfaces.IErrorResponseHandler;
import com.ecommerce.api_gateway.Filter.interfaces.IRouteExclusionService;
import com.ecommerce.api_gateway.Filter.interfaces.ITokenValidationService;
import com.ecommerce.api_gateway.constants.GatewayConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Tag(name = "Authentication Filter", description = "Global Gateway Filter verifying JWT integrity and claims for secure endpoints")
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final ITokenValidationService tokenValidationService;
    private final IRouteExclusionService routeExclusionService;
    private final IErrorResponseHandler errorResponseHandler;
    private final IGatewayLogger logger;

    public AuthenticationFilter(
            ITokenValidationService tokenValidationService,
            IRouteExclusionService routeExclusionService,
            IErrorResponseHandler errorResponseHandler,
            IGatewayLogger logger) {
        this.tokenValidationService = tokenValidationService;
        this.routeExclusionService = routeExclusionService;
        this.errorResponseHandler = errorResponseHandler;
        this.logger = logger;
    }

    @Override
    @Operation(summary = "Validate incoming JWT tokens", description = "Intercepts the incoming request, checks if the path is excluded or if it is an OPTIONS pre-flight request. If the path requires authentication, it extracts the Bearer JWT token, validates its signature and expiration, and binds user claims (ID, roles) as downstream headers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT Token is valid, forwarding user headers downstream"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Missing/invalid header, token expired, signature failure, or malformed JWT")
    })
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String correlationId = exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR);

        if (request.getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/webjars")) {
            return chain.filter(exchange);
        }

        if (routeExclusionService.isExcluded(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(GatewayConstants.BEARER_PREFIX)) {
            logger.warn(AuthenticationFilter.class, "Missing or invalid Authorization header. path={}, correlationId={}", path, correlationId);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(GatewayConstants.BEARER_PREFIX.length());

        if (token.isBlank()) {
            logger.warn(AuthenticationFilter.class, "Missing or invalid Authorization header (empty token). path={}, correlationId={}", path, correlationId);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        try {
            Claims claims = tokenValidationService.validateToken(token);

            String userId = claims.getSubject();
            String roles = claims.get(GatewayConstants.ROLES_CLAIM, String.class);

            if (userId == null) {
                logger.warn(AuthenticationFilter.class, "JWT missing subject claim. correlationId={}", correlationId);
                return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token: missing subject");
            }

            exchange.getAttributes().put(GatewayConstants.USER_ID_ATTR, userId);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove(GatewayConstants.USER_ID_HEADER);
                        headers.remove(GatewayConstants.USER_ROLES_HEADER);

                        headers.set(GatewayConstants.USER_ID_HEADER, userId);
                        if (roles != null) {
                            headers.set(GatewayConstants.USER_ROLES_HEADER, roles);
                        }
                    })
                    .build();

            logger.debug(AuthenticationFilter.class, "JWT validated. userId={}, roles={}, path={}, correlationId={}", userId, roles, path, correlationId);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            logger.info(AuthenticationFilter.class, "JWT expired. correlationId={}", correlationId);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token expired");
        } catch (MalformedJwtException e) {
            logger.warn(AuthenticationFilter.class, "Malformed JWT. correlationId={}", correlationId);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token format");
        } catch (SignatureException e) {
            logger.warn(AuthenticationFilter.class, "JWT signature verification failed. correlationId={}", correlationId);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid token signature");
        } catch (Exception e) {
            logger.error(AuthenticationFilter.class, "JWT validation failed unexpectedly. correlationId={}", correlationId, e);
            return errorResponseHandler.sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
