package com.ecommerce.catalog_service.Dto.Request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name,
        @NotBlank String slug,
        String parentId,
        String description
) {}
