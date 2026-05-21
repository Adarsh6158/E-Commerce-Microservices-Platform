package com.ecommerce.api_gateway.Config;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FallbackControllerTest {

    @Mock
    private IGatewayLogger logger;

    private FallbackController fallbackController;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        fallbackController = new FallbackController(logger);
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("correlationId", "test-corr-id");
    }

    @Test
    @DisplayName("Should build generic fallback response correctly for any service")
    void fallback_whenCalled_shouldReturn503AndCorrectBody() {
        StepVerifier.create(fallbackController.authFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Auth Service"))
                .verifyComplete();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        verify(logger).warn(eq(FallbackController.class), any(), eq("Auth Service"), eq("test-corr-id"));
    }

    @Test
    @DisplayName("Should return catalog fallback response")
    void catalogFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.catalogFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Catalog Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return search fallback response")
    void searchFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.searchFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Search Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return cart fallback response")
    void cartFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.cartFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Cart Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return pricing fallback response")
    void pricingFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.pricingFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Pricing Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return inventory fallback response")
    void inventoryFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.inventoryFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Inventory Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return order fallback response")
    void orderFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.orderFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Order Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return payment fallback response")
    void paymentFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.paymentFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Payment Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return notification fallback response")
    void notificationFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.notificationFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Notification Service"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return analytics fallback response")
    void analyticsFallback_whenCalled_shouldReturn503() {
        StepVerifier.create(fallbackController.analyticsFallback(exchange))
                .assertNext(body -> assertFallbackBody(body, "Analytics Service"))
                .verifyComplete();
    }

    private void assertFallbackBody(Map<String, Object> body, String serviceName) {
        assertEquals(503, body.get("status"));
        assertEquals("Service Unavailable", body.get("error"));
        assertEquals("test-corr-id", body.get("correlationId"));
        assertEquals(serviceName + " is temporarily unavailable. Please retry later.", body.get("message"));
    }
}
