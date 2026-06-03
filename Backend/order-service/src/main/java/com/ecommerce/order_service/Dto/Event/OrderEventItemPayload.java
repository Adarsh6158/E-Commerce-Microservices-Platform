package com.ecommerce.order_service.Dto.Event;

import java.math.BigDecimal;

public record OrderEventItemPayload(
        String productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {}
