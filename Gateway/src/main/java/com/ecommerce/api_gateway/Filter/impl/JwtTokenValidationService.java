package com.ecommerce.api_gateway.Filter.impl;

import com.ecommerce.api_gateway.Filter.interfaces.ITokenValidationService;

import com.ecommerce.api_gateway.security.JwtUtil;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenValidationService implements ITokenValidationService {

    private final JwtUtil jwtUtil;

    public JwtTokenValidationService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Claims validateToken(String token) {
        return jwtUtil.validateTokenAndGetClaims(token);
    }
}
