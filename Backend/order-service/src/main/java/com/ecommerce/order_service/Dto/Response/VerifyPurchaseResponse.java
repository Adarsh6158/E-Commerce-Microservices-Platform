package com.ecommerce.order_service.Dto.Response;

public record VerifyPurchaseResponse(
        String userId,
        String productId,
        boolean purchased
) {}
