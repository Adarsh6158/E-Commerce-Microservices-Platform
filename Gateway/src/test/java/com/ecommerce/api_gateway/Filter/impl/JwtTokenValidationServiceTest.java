package com.ecommerce.api_gateway.Filter.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import com.ecommerce.api_gateway.security.JwtUtil;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    private JwtTokenValidationService tokenValidationService;

    @BeforeEach
    void setUp() {
        tokenValidationService = new JwtTokenValidationService(jwtUtil);
    }

    @Test
    @DisplayName("Should return claims when token is valid")
    void validateToken_whenValid_shouldReturnClaims() {
        Claims mockClaims = mock(Claims.class);
        when(jwtUtil.validateTokenAndGetClaims("valid")).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn("user-1");

        Claims claims = tokenValidationService.validateToken("valid");
        assertEquals("user-1", claims.getSubject());
    }

    @Test
    @DisplayName("Should propagate ExpiredJwtException")
    void validateToken_whenExpired_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("expired")).thenThrow(new ExpiredJwtException(null, null, "Exp"));
        assertThrows(ExpiredJwtException.class, () -> tokenValidationService.validateToken("expired"));
    }

    @Test
    @DisplayName("Should propagate MalformedJwtException")
    void validateToken_whenMalformed_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("malformed")).thenThrow(new MalformedJwtException("Mal"));
        assertThrows(MalformedJwtException.class, () -> tokenValidationService.validateToken("malformed"));
    }

    @Test
    @DisplayName("Should propagate SignatureException")
    void validateToken_whenInvalidSig_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("bad-sig")).thenThrow(new SignatureException("Sig"));
        assertThrows(SignatureException.class, () -> tokenValidationService.validateToken("bad-sig"));
    }

    @Test
    @DisplayName("Should propagate UnsupportedJwtException")
    void validateToken_whenUnsupported_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("unsup")).thenThrow(new UnsupportedJwtException("Unsup"));
        assertThrows(UnsupportedJwtException.class, () -> tokenValidationService.validateToken("unsup"));
    }

    @Test
    @DisplayName("Should propagate IllegalArgumentException for null token")
    void validateToken_whenNull_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims(null)).thenThrow(new IllegalArgumentException("Null"));
        assertThrows(IllegalArgumentException.class, () -> tokenValidationService.validateToken(null));
    }

    @Test
    @DisplayName("Should propagate IllegalArgumentException for empty token")
    void validateToken_whenEmpty_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("")).thenThrow(new IllegalArgumentException("Empty"));
        assertThrows(IllegalArgumentException.class, () -> tokenValidationService.validateToken(""));
    }

    @Test
    @DisplayName("Should propagate IllegalArgumentException for blank token")
    void validateToken_whenBlank_shouldThrow() {
        when(jwtUtil.validateTokenAndGetClaims("   ")).thenThrow(new IllegalArgumentException("Blank"));
        assertThrows(IllegalArgumentException.class, () -> tokenValidationService.validateToken("   "));
    }
}
