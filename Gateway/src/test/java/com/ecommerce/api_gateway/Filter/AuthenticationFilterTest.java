package com.ecommerce.api_gateway.Filter;

import com.ecommerce.api_gateway.Config.interfaces.IGatewayLogger;
import com.ecommerce.api_gateway.Exception.interfaces.IErrorResponseHandler;
import com.ecommerce.api_gateway.Filter.interfaces.IRouteExclusionService;
import com.ecommerce.api_gateway.Filter.interfaces.ITokenValidationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

        @Mock
        private ITokenValidationService tokenValidationService;

        @Mock
        private IRouteExclusionService routeExclusionService;

        @Mock
        private IErrorResponseHandler errorResponseHandler;

        @Mock
        private IGatewayLogger logger;

        @Mock
        private GatewayFilterChain filterChain;

        @Mock
        private Claims claims;

        private AuthenticationFilter authenticationFilter;
        private MockServerWebExchange exchange;

        @BeforeEach
        void setUp() {
                authenticationFilter = new AuthenticationFilter(
                                tokenValidationService, routeExclusionService, errorResponseHandler, logger);

                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured").build();
                exchange = MockServerWebExchange.from(request);
                exchange.getAttributes().put("correlationId", "test-123");
        }

        @Test
        @DisplayName("Should skip filter for OPTIONS requests (CORS preflight)")
        void filter_whenOptionsRequest_shouldPassThrough() {
                MockServerHttpRequest request = MockServerHttpRequest.options("/api/secured").build();
                MockServerWebExchange optionsExchange = MockServerWebExchange.from(request);

                when(filterChain.filter(optionsExchange)).thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(optionsExchange, filterChain))
                                .verifyComplete();

                verify(filterChain).filter(optionsExchange);
                verifyNoInteractions(routeExclusionService, tokenValidationService);
        }

        @Test
        @DisplayName("Should skip filter for excluded paths")
        void filter_whenExcludedPath_shouldPassThrough() {
                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(true);
                when(filterChain.filter(exchange)).thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(filterChain).filter(exchange);
                verifyNoInteractions(tokenValidationService);
        }

        @Test
        @DisplayName("Should return 401 when Authorization header is missing")
        void filter_whenAuthHeaderMissing_shouldReturn401() {
                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED), anyString()))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                                "Missing or invalid Authorization header");
        }

        @ParameterizedTest
        @MethodSource("provideInvalidAuthHeaders")
        @DisplayName("Should return 401 when Authorization header is incorrectly formatted")
        void filter_whenAuthHeaderInvalid_shouldReturn401(String invalidHeader) {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, invalidHeader)
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED), anyString()))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                                "Missing or invalid Authorization header");
        }

        @Test
        @DisplayName("Should return 401 when JWT subject is missing")
        void filter_whenJwtMissingSubject_shouldReturn401() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("valid.jwt.token")).thenReturn(claims);
                when(claims.getSubject()).thenReturn(null);
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED), anyString()))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                                "Invalid token: missing subject");
        }

        @Test
        @DisplayName("Should return 401 when JWT is expired")
        void filter_whenJwtExpired_shouldReturn401() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer expired.jwt.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("expired.jwt.token"))
                                .thenThrow(new ExpiredJwtException(null, null, "Expired"));
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED), eq("Token expired")))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(errorResponseHandler).sendErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Token expired");
        }

        @Test
        @DisplayName("Should return 401 when JWT is malformed")
        void filter_whenJwtMalformed_shouldReturn401() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer malformed.jwt.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("malformed.jwt.token"))
                                .thenThrow(new MalformedJwtException("Malformed"));
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED),
                                eq("Invalid token format")))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return 401 when JWT signature is invalid")
        void filter_whenJwtSignatureInvalid_shouldReturn401() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.sig.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("invalid.sig.token"))
                                .thenThrow(new SignatureException("Invalid Sig"));
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED),
                                eq("Invalid token signature")))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();
        }

        @Test
        @DisplayName("Should return 401 when an unexpected exception occurs during validation")
        void filter_whenUnexpectedException_shouldReturn401() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer random.jwt.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("random.jwt.token"))
                                .thenThrow(new RuntimeException("Unexpected error"));
                when(errorResponseHandler.sendErrorResponse(any(), eq(HttpStatus.UNAUTHORIZED),
                                eq("Authentication failed")))
                                .thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();
        }

        @ParameterizedTest
        @MethodSource("provideValidJwtRoles")
        @DisplayName("Should mutate request and pass through when JWT is valid, with or without roles")
        void filter_whenJwtValid_shouldMutateRequestAndPassThrough(String role) {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/secured")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt.token")
                                .build();
                exchange = MockServerWebExchange.from(request);

                when(routeExclusionService.isExcluded("/api/secured")).thenReturn(false);
                when(tokenValidationService.validateToken("valid.jwt.token")).thenReturn(claims);
                when(claims.getSubject()).thenReturn("user-1");
                when(claims.get("roles", String.class)).thenReturn(role);
                when(filterChain.filter(any())).thenReturn(Mono.empty());

                StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                assertEquals("user-1", exchange.getAttribute("userId"));
                verify(filterChain).filter(argThat(mutatedExchange -> {
                        boolean userMatch = "user-1"
                                        .equals(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
                        boolean roleMatch = role == null || role
                                        .equals(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Roles"));
                        return userMatch && roleMatch;
                }));
        }

        @Test
        @DisplayName("Should ensure getOrder returns HIGHEST_PRECEDENCE + 1")
        void getOrder_shouldReturnCorrectOrder() {
                assertEquals(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1, authenticationFilter.getOrder());
        }

        private static Stream<String> provideInvalidAuthHeaders() {
                return Stream.of("Basic dXNlcjpwYXNz", "BearerTokenWithoutSpace", "Bearer ", "bearer jwt.token.here");
        }

        private static Stream<String> provideValidJwtRoles() {
                return Stream.of("ROLE_USER", "ROLE_ADMIN", null);
        }
}
