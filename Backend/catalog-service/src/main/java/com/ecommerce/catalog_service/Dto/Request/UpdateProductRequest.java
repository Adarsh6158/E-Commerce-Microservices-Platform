package com.ecommerce.catalog_service.Dto.Request;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateProductRequest(
        String name,
        String description,
        String categoryId,
        String brand,
        BigDecimal basePrice,
        @jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid image URL") String image,
        @jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid thumbnail URL") String thumbnail,
        java.util.List<@jakarta.validation.constraints.Pattern(regexp = "^(http|https)://.*", message = "Invalid gallery image URL") String> galleryImages,
        String altText,
        Boolean active,
        Double weight,
        Map<String, Object> attributes
) {}
