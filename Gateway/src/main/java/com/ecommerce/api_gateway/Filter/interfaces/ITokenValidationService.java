package com.ecommerce.api_gateway.Filter.interfaces;

import io.jsonwebtoken.Claims;

public interface ITokenValidationService {
    Claims validateToken(String token);
}
