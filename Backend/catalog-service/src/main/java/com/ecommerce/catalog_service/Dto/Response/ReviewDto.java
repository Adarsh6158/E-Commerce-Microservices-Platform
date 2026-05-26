package com.ecommerce.catalog_service.Dto.Response;

import java.time.Instant;

public record ReviewDto(
        String id,
        String productId,
        String userId,
        String userName,
        int rating,
        String title,
        String comment,
        boolean verified,
        Instant createdAt
) {}
