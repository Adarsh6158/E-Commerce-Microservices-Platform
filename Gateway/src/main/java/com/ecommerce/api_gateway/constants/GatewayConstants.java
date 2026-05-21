package com.ecommerce.api_gateway.constants;

public final class GatewayConstants {

    private GatewayConstants() {
    }

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_ATTR = "correlationId";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_ID_ATTR = "userId";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLES_CLAIM = "roles";
}

