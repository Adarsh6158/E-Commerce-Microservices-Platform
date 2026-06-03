package com.ecommerce.order_service.Dto.Response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
