package com.ecommerce.order_service.Dto.Response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
        String userId,
        String status,
        BigDecimal totalAmount,
        String currency,
        String shippingAddress,
        String correlationId,
        String failureReason,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
