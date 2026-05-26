package com.ecommerce.catalog_service.Dto.Response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductDto(
        String id,
        String sku,
        String name,
        String description,
        String categoryId,
        String brand,
        BigDecimal basePrice,
        String image,
        String thumbnail,
        List<String> galleryImages,
        String altText,
        boolean active,
        Double weight,
        Map<String, Object> attributes,
        Instant createdAt,
        Instant updatedAt
) {}
