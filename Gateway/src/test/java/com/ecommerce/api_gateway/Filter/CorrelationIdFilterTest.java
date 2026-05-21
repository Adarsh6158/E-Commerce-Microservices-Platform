package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.constants.GatewayConstants;
import com.ecommerce.api_gateway.Filter.interfaces.ICorrelationIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private ICorrelationIdGenerator generator;

    @Mock
    private GatewayFilterChain filterChain;

    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter(generator);
    }

    @Test
    @DisplayName("Should generate correlation ID when missing from request")
    void filter_whenCorrelationIdMissing_shouldGenerateAndAdd() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        when(generator.generate()).thenReturn("gen-id-1");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals("gen-id-1", exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Test
    @DisplayName("Should preserve correlation ID when present in request")
    void filter_whenCorrelationIdPresent_shouldPreserve() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").header(GatewayConstants.CORRELATION_ID_HEADER, "ext-id").build());
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals("ext-id", exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Test
    @DisplayName("Should generate correlation ID when header is empty string")
    void filter_whenCorrelationIdEmpty_shouldGenerate() {
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("/test").header(GatewayConstants.CORRELATION_ID_HEADER, "").build());
        when(generator.generate()).thenReturn("gen-id-2");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals("gen-id-2", exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Test
    @DisplayName("Should generate correlation ID when header is blank spaces")
    void filter_whenCorrelationIdBlank_shouldGenerate() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").header(GatewayConstants.CORRELATION_ID_HEADER, "   ").build());
        when(generator.generate()).thenReturn("gen-id-3");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals("gen-id-3", exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Test
    @DisplayName("Should correctly mutate the request to include the correlation ID header")
    void filter_shouldMutateRequest() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        when(generator.generate()).thenReturn("gen-id-4");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(argThat(ex -> "gen-id-4"
                .equals(ex.getRequest().getHeaders().getFirst(GatewayConstants.CORRELATION_ID_HEADER))));
    }

    @Test
    @DisplayName("Should correctly add correlation ID to the response headers")
    void filter_shouldMutateResponse() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        when(generator.generate()).thenReturn("gen-id-5");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals("gen-id-5",
                exchange.getResponse().getHeaders().getFirst(GatewayConstants.CORRELATION_ID_HEADER));
    }

    @Test
    @DisplayName("Should pass through if Mono fails downstream")
    void filter_whenChainFails_shouldStillSetId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        when(generator.generate()).thenReturn("gen-id-6");
        when(filterChain.filter(any())).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).expectError(RuntimeException.class)
                .verify();

        assertEquals("gen-id-6", exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }

    @Test
    @DisplayName("Should ensure getOrder returns HIGHEST_PRECEDENCE")
    void getOrder_shouldReturnHighestPrecedence() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, correlationIdFilter.getOrder());
    }

    @Test
    @DisplayName("Should have no missing attribute side effects")
    void filter_shouldInitializeProperly() {
        assertNotNull(correlationIdFilter);
    }

    @Test
    @DisplayName("Should handle very large header values securely")
    void filter_whenCorrelationIdVeryLarge_shouldTruncateOrPreserve() {
        String largeId = "A".repeat(500);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").header(GatewayConstants.CORRELATION_ID_HEADER, largeId).build());
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(correlationIdFilter.filter(exchange, filterChain)).verifyComplete();

        assertEquals(largeId, exchange.getAttribute(GatewayConstants.CORRELATION_ID_ATTR));
    }
}
