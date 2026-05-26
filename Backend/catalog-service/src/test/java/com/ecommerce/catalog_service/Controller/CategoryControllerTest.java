package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Mapper.CatalogMapperImpl;
import com.ecommerce.catalog_service.Service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private CategoryService categoryService;

    private CatalogMapper catalogMapper;
    private CategoryController controller;

    @BeforeEach
    void setUp() {
        catalogMapper = new CatalogMapperImpl();
        controller = new CategoryController(categoryService, catalogMapper);
    }

    @Test
    void getAll_returnsCategories() {
        when(categoryService.getAllActive())
                .thenReturn(Flux.just(buildCategory("c1"), buildCategory("c2")));

        StepVerifier.create(controller.getAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getById_returnsCategory() {
        when(categoryService.getById("c1")).thenReturn(Mono.just(buildCategory("c1")));

        StepVerifier.create(controller.getById("c1"))
                .expectNextMatches(dto -> "c1".equals(dto.id()))
                .verifyComplete();
    }

    @Test
    void getBySlug_returnsCategory() {
        Category c = buildCategory("c1");
        c.setSlug("electronics");
        when(categoryService.getBySlug("electronics")).thenReturn(Mono.just(c));

        StepVerifier.create(controller.getBySlug("electronics"))
                .expectNextMatches(dto -> "electronics".equals(dto.slug()))
                .verifyComplete();
    }

    @Test
    void getChildren_returnsChildCategories() {
        when(categoryService.getChildren("parent"))
                .thenReturn(Flux.just(buildCategory("c1")));

        StepVerifier.create(controller.getChildren("parent"))
                .expectNextCount(1)
                .verifyComplete();
    }



    private Category buildCategory(String id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Cat " + id);
        c.setSlug("slug-" + id);
        c.setActive(true);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
