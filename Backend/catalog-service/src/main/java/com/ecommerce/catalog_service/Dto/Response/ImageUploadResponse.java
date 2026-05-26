package com.ecommerce.catalog_service.Dto.Response;

import java.util.List;

public record ImageUploadResponse(
        String productId,
        List<String> galleryImages
) {}
