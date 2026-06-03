package com.ecommerce.order_service.Dto.Event;

public record PaymentEventPayload(
        String orderId,
        String reason
) {}
