package com.ecommerce.catalog_service.Dto.Response;

import java.time.Instant;

public record CategoryDto(
        String id,
        String name,
        String slug,
        String parentId,
        String description,
        boolean active,
        Instant createdAt
) {}
