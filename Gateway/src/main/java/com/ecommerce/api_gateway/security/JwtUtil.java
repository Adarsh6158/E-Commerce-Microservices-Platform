package com.ecommerce.api_gateway.security;

import com.ecommerce.api_gateway.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private JwtParser jwtParser;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        SecretKey jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build();
    }

    public Claims validateTokenAndGetClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
}
