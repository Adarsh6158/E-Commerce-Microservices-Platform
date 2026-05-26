package com.ecommerce.catalog_service.Mapper;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.*;
import com.ecommerce.catalog_service.Dto.Response.*;

public interface CatalogMapper {

    ProductDto toProductDto(Product product);

    Product toEntity(CreateProductRequest request);

    Product applyUpdates(Product existing, UpdateProductRequest updates);

    CategoryDto toCategoryDto(Category category);

    Category toEntity(CreateCategoryRequest request);

    ReviewDto toReviewDto(Review review);
}
