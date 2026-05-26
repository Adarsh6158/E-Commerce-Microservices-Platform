package com.ecommerce.catalog_service.Dto.Response;

public record ProductRatingResponse(
        String productId,
        double averageRating,
        long reviewCount
) {}
