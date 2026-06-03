package com.ecommerce.order_service.Dto.Event;

import java.math.BigDecimal;
import java.util.List;

public record OrderEventPayload(
        String orderId,
        String userId,
        String status,
        BigDecimal totalAmount,
        String correlationId,
        String failureReason,
        List<OrderEventItemPayload> items
) {}
