package com.ecommerce.api_gateway.Config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

class RateLimiterConfigTest {

    private final RateLimiterConfig rateLimiterConfig = new RateLimiterConfig();

    @Test
    @DisplayName("Should resolve IP address for ipKeyResolver")
    void ipKeyResolver_whenIpIsAvailable_shouldReturnIp() {
        KeyResolver resolver = rateLimiterConfig.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .remoteAddress(new InetSocketAddress("192.168.1.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("192.168.1.1")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should resolve unknown for ipKeyResolver when IP is missing")
    void ipKeyResolver_whenIpIsMissing_shouldReturnUnknown() {
        KeyResolver resolver = rateLimiterConfig.ipKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("unknown")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should resolve userId for userKeyResolver when userId attribute is present")
    void userKeyResolver_whenUserIdIsPresent_shouldReturnUserId() {
        KeyResolver resolver = rateLimiterConfig.userKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("userId", "user-123");

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("user-123")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fallback to IP for userKeyResolver when userId is missing")
    void userKeyResolver_whenUserIdIsMissing_shouldFallbackToIp() {
        KeyResolver resolver = rateLimiterConfig.userKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("10.0.0.1")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fallback to unknown for userKeyResolver when both userId and IP are missing")
    void userKeyResolver_whenBothMissing_shouldReturnUnknown() {
        KeyResolver resolver = rateLimiterConfig.userKeyResolver();
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("unknown")
                .verifyComplete();
    }
}
