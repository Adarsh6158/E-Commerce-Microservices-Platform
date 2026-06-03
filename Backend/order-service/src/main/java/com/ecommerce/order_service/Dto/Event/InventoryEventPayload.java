package com.ecommerce.order_service.Dto.Event;

public record InventoryEventPayload(
        String orderId,
        String reason
) {}
