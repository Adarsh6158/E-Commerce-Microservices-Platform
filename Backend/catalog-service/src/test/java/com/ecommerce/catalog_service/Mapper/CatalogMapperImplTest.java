package com.ecommerce.catalog_service.Mapper;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.*;
import com.ecommerce.catalog_service.Dto.Response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogMapperImplTest {

    private CatalogMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new CatalogMapperImpl();
    }



    @Test
    void toProductDto_mapsAllFields() {
        Product p = buildProduct();
        ProductDto dto = mapper.toProductDto(p);

        assertEquals("p1", dto.id());
        assertEquals("SKU-001", dto.sku());
        assertEquals("Test Product", dto.name());
        assertEquals("Desc", dto.description());
        assertEquals("cat1", dto.categoryId());
        assertEquals("BrandX", dto.brand());
        assertEquals(new BigDecimal("29.99"), dto.basePrice());
        assertEquals("img.jpg", dto.image());
        assertEquals("thumb.jpg", dto.thumbnail());
        assertEquals(List.of("g1.jpg", "g2.jpg"), dto.galleryImages());
        assertEquals("alt text", dto.altText());
        assertTrue(dto.active());
        assertEquals(1.5, dto.weight());
        assertEquals(Map.of("color", "red"), dto.attributes());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
    }

    @Test
    void toProductDto_handlesNullFields() {
        Product p = new Product();
        ProductDto dto = mapper.toProductDto(p);

        assertNull(dto.id());
        assertNull(dto.sku());
        assertNull(dto.name());
        assertNull(dto.galleryImages());
        assertNull(dto.attributes());
        assertFalse(dto.active());
    }



    @Test
    void toEntity_createProductRequest_mapsAllFields() {
        CreateProductRequest request = new CreateProductRequest(
                "SKU-002", "New Product", "New Desc", "cat2", "BrandY",
                new BigDecimal("49.99"), "img2.jpg", "thumb2.jpg",
                List.of("g3.jpg"), "alt2", 2.0, Map.of("size", "L")
        );

        Product p = mapper.toEntity(request);

        assertEquals("SKU-002", p.getSku());
        assertEquals("New Product", p.getName());
        assertEquals("New Desc", p.getDescription());
        assertEquals("cat2", p.getCategoryId());
        assertEquals("BrandY", p.getBrand());
        assertEquals(new BigDecimal("49.99"), p.getBasePrice());
        assertEquals("img2.jpg", p.getImage());
        assertEquals("thumb2.jpg", p.getThumbnail());
        assertEquals(List.of("g3.jpg"), p.getGalleryImages());
        assertEquals("alt2", p.getAltText());
        assertEquals(2.0, p.getWeight());
        assertEquals(Map.of("size", "L"), p.getAttributes());
    }



    @Test
    void applyUpdates_onlyNonNullFieldsApplied() {
        Product existing = buildProduct();
        UpdateProductRequest updates = new UpdateProductRequest(
                "Updated Name", null, null, null, new BigDecimal("39.99"),
                null, null, null, null, null, null, null
        );

        Product result = mapper.applyUpdates(existing, updates);

        assertEquals("Updated Name", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals(new BigDecimal("39.99"), result.getBasePrice());
        assertEquals("BrandX", result.getBrand());
    }

    @Test
    void applyUpdates_allNullFieldsPreservesExisting() {
        Product existing = buildProduct();
        UpdateProductRequest updates = new UpdateProductRequest(
                null, null, null, null, null, null, null, null, null, null, null, null
        );

        Product result = mapper.applyUpdates(existing, updates);

        assertEquals("Test Product", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals(new BigDecimal("29.99"), result.getBasePrice());
    }



    @Test
    void toCategoryDto_mapsAllFields() {
        Category c = new Category();
        c.setId("c1");
        c.setName("Electronics");
        c.setSlug("electronics");
        c.setParentId("root");
        c.setDescription("Electronic items");
        c.setActive(true);
        c.setCreatedAt(Instant.now());

        CategoryDto dto = mapper.toCategoryDto(c);

        assertEquals("c1", dto.id());
        assertEquals("Electronics", dto.name());
        assertEquals("electronics", dto.slug());
        assertEquals("root", dto.parentId());
        assertEquals("Electronic items", dto.description());
        assertTrue(dto.active());
        assertNotNull(dto.createdAt());
    }



    @Test
    void toEntity_createCategoryRequest_mapsAllFields() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Books", "books", "root", "All books"
        );

        Category c = mapper.toEntity(request);

        assertEquals("Books", c.getName());
        assertEquals("books", c.getSlug());
        assertEquals("root", c.getParentId());
        assertEquals("All books", c.getDescription());
    }



    @Test
    void toReviewDto_mapsAllFields() {
        Review r = new Review();
        r.setId("r1");
        r.setProductId("p1");
        r.setUserId("u1");
        r.setUserName("John");
        r.setRating(5);
        r.setTitle("Great");
        r.setComment("Loved it");
        r.setVerified(true);
        r.setCreatedAt(Instant.now());

        ReviewDto dto = mapper.toReviewDto(r);

        assertEquals("r1", dto.id());
        assertEquals("p1", dto.productId());
        assertEquals("u1", dto.userId());
        assertEquals("John", dto.userName());
        assertEquals(5, dto.rating());
        assertEquals("Great", dto.title());
        assertEquals("Loved it", dto.comment());
        assertTrue(dto.verified());
        assertNotNull(dto.createdAt());
    }



    private Product buildProduct() {
        Product p = new Product();
        p.setId("p1");
        p.setSku("SKU-001");
        p.setName("Test Product");
        p.setDescription("Desc");
        p.setCategoryId("cat1");
        p.setBrand("BrandX");
        p.setBasePrice(new BigDecimal("29.99"));
        p.setImage("img.jpg");
        p.setThumbnail("thumb.jpg");
        p.setGalleryImages(List.of("g1.jpg", "g2.jpg"));
        p.setAltText("alt text");
        p.setActive(true);
        p.setWeight(1.5);
        p.setAttributes(Map.of("color", "red"));
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }
}
