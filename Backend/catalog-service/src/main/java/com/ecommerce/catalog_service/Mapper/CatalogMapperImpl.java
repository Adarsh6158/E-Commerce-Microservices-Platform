package com.ecommerce.catalog_service.Mapper;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.*;
import com.ecommerce.catalog_service.Dto.Response.*;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapperImpl implements CatalogMapper {

    @Override
    public ProductDto toProductDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getCategoryId(),
                p.getBrand(),
                p.getBasePrice(),
                p.getImage(),
                p.getThumbnail(),
                p.getGalleryImages(),
                p.getAltText(),
                p.isActive(),
                p.getWeight(),
                p.getAttributes(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    @Override
    public Product toEntity(CreateProductRequest r) {
        Product p = new Product();
        p.setSku(r.sku());
        p.setName(r.name());
        p.setDescription(r.description());
        p.setCategoryId(r.categoryId());
        p.setBrand(r.brand());
        p.setBasePrice(r.basePrice());
        p.setImage(r.image());
        p.setThumbnail(r.thumbnail());
        p.setGalleryImages(r.galleryImages());
        p.setAltText(r.altText());
        p.setWeight(r.weight());
        p.setAttributes(r.attributes());
        return p;
    }

    @Override
    public Product applyUpdates(Product existing, UpdateProductRequest updates) {
        if (updates.name() != null) existing.setName(updates.name());
        if (updates.description() != null) existing.setDescription(updates.description());
        if (updates.categoryId() != null) existing.setCategoryId(updates.categoryId());
        if (updates.brand() != null) existing.setBrand(updates.brand());
        if (updates.basePrice() != null) existing.setBasePrice(updates.basePrice());
        if (updates.image() != null) existing.setImage(updates.image());
        if (updates.thumbnail() != null) existing.setThumbnail(updates.thumbnail());
        if (updates.galleryImages() != null) existing.setGalleryImages(updates.galleryImages());
        if (updates.altText() != null) existing.setAltText(updates.altText());
        if (updates.active() != null) existing.setActive(updates.active());
        if (updates.weight() != null) existing.setWeight(updates.weight());
        if (updates.attributes() != null) existing.setAttributes(updates.attributes());
        return existing;
    }

    @Override
    public CategoryDto toCategoryDto(Category c) {
        return new CategoryDto(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getParentId(),
                c.getDescription(),
                c.isActive(),
                c.getCreatedAt()
        );
    }

    @Override
    public Category toEntity(CreateCategoryRequest r) {
        Category c = new Category();
        c.setName(r.name());
        c.setSlug(r.slug());
        c.setParentId(r.parentId());
        c.setDescription(r.description());
        return c;
    }

    @Override
    public ReviewDto toReviewDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getProductId(),
                r.getUserId(),
                r.getUserName(),
                r.getRating(),
                r.getTitle(),
                r.getComment(),
                r.isVerified(),
                r.getCreatedAt()
        );
    }
}
