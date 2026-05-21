package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private IGatewayLogger logger;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    @Mock
    private GatewayFilterChain filterChain;

    private RequestLoggingFilter loggingFilter;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        loggingFilter = new RequestLoggingFilter(logger, meterRegistry);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test?query=1")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put("correlationId", "test-corr");
        exchange.getAttributes().put("userId", "user-1");
        
        lenient().when(meterRegistry.timer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(timer);
    }

    @Test
    @DisplayName("Should log request entry and 200 OK response exit")
    void filter_whenSuccess_shouldLogInfo() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.OK)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).info(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(200), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 201 CREATED response exit")
    void filter_whenCreated_shouldLogInfo() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.CREATED)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).info(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(201), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 204 NO CONTENT response exit")
    void filter_whenNoContent_shouldLogInfo() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).info(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(204), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 400 BAD REQUEST response exit as warning")
    void filter_whenBadRequest_shouldLogWarn() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).warn(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(400), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 401 UNAUTHORIZED response exit as warning")
    void filter_whenUnauthorized_shouldLogWarn() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).warn(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(401), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 403 FORBIDDEN response exit as warning")
    void filter_whenForbidden_shouldLogWarn() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).warn(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(403), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 404 NOT FOUND response exit as warning")
    void filter_whenNotFound_shouldLogWarn() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).warn(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(404), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should log request entry and 500 INTERNAL SERVER ERROR response exit as error")
    void filter_whenServerError_shouldLogError() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).error(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(500), anyLong(), eq("test-corr"));
    }

    @Test
    @DisplayName("Should handle missing remote address gracefully")
    void filter_whenMissingRemoteAddress_shouldLogUnknown() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty().doOnSuccess(v -> exchange.getResponse().setStatusCode(HttpStatus.OK)).then());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).info(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq((Object)null), eq("unknown"), eq((Object)null), eq((Object)null));
    }

    @Test
    @DisplayName("Should handle missing status code gracefully")
    void filter_whenMissingStatusCode_shouldLogZero() {
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
        verify(logger).info(eq(RequestLoggingFilter.class), anyString(), eq("GET"), eq("/api/test"), eq(0), anyLong(), eq("test-corr"));
    }
    
    @Test
    @DisplayName("Should return correct precedence order")
    void getOrder_shouldReturnCorrectOrder() {
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 10, loggingFilter.getOrder());
    }
}
