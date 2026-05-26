package com.ecommerce.catalog_service.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        String categoryId,
        String brand,
        @NotNull BigDecimal basePrice,
        @jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid image URL") String image,
        @jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid thumbnail URL") String thumbnail,
        java.util.List<@jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid gallery image URL") String> galleryImages,
        String altText,
        Double weight,
        Map<String, Object> attributes
) {}
