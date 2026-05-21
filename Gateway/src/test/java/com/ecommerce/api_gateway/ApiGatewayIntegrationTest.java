package com.ecommerce.api_gateway;

import com.ecommerce.api_gateway.Filter.interfaces.ITokenValidationService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ApiGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.cloud.gateway.routes[0].id=test-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:9999",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/test/**",
        "spring.cloud.gateway.routes[0].filters[0].name=CircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.name=testCircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.fallbackUri=forward:/fallback/auth",
        "spring.cloud.gateway.routes[0].filters[1].name=RequestRateLimiter",
        "spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.replenishRate=1",
        "spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.burstCapacity=1",
        "spring.cloud.gateway.routes[0].filters[1].args.key-resolver=#{@ipKeyResolver}",
        "gateway.auth.excluded-paths=/api/test/public"
})
@Import(ApiGatewayIntegrationTest.TestConfig.class)
public class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ITokenValidationService tokenValidationService;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Configuration
    static class TestConfig {
        // We do not need extra beans for this test, but we can override if needed
    }

    @Test
    @DisplayName("Should block request when JWT is missing on secured route")
    void shouldBlockMissingJwt() {
        webClient.get().uri("/api/test/secure")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Unauthorized")
                .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header");
    }

    @Test
    @DisplayName("Should pass request when JWT is valid and trigger circuit breaker fallback due to unreachable downstream")
    void shouldPassJwtAndTriggerFallback() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-123");
        when(tokenValidationService.validateToken("valid-token")).thenReturn(claims);

        // Mock Rate Limiter to allow
        when(redisRateLimiter.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(
                        new org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response(true, Map.of())));

        // The downstream http://localhost:9999 is unreachable, so CircuitBreaker will
        // trigger fallback /fallback/auth
        webClient.get().uri("/api/test/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").isEqualTo("Auth Service is temporarily unavailable. Please retry later.");
    }

    @Test
    @DisplayName("Should return 429 Too Many Requests when rate limit is exceeded")
    void shouldTriggerRateLimit() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-123");
        when(tokenValidationService.validateToken("valid-token")).thenReturn(claims);

        // Mock Rate Limiter to REJECT (rate limit exceeded)
        when(redisRateLimiter.isAllowed(anyString(), anyString()))
                .thenReturn(Mono.just(
                        new org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response(false, Map.of())));

        webClient.get().uri("/api/test/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .exchange()
                .expectStatus().isEqualTo(429);
    }
}
