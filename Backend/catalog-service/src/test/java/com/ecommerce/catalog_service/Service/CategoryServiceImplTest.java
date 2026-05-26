package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Repository.CategoryRepository;
import com.ecommerce.catalog_service.Service.Impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository);
    }

    @Test
    void getAllActive_returnsActiveCategories() {
        when(categoryRepository.findByActiveTrue())
                .thenReturn(Flux.just(buildCategory("c1"), buildCategory("c2")));

        StepVerifier.create(service.getAllActive())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllActive_emptyList_returnsEmpty() {
        when(categoryRepository.findByActiveTrue()).thenReturn(Flux.empty());

        StepVerifier.create(service.getAllActive())
                .verifyComplete();
    }

    @Test
    void getById_found() {
        Category category = buildCategory("c1");
        when(categoryRepository.findById("c1")).thenReturn(Mono.just(category));

        StepVerifier.create(service.getById("c1"))
                .expectNextMatches(c -> "c1".equals(c.getId()))
                .verifyComplete();
    }

    @Test
    void getById_notFound_returnsEmpty() {
        when(categoryRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.getById("missing"))
                .verifyComplete();
    }

    @Test
    void getBySlug_found() {
        Category category = buildCategory("c1");
        category.setSlug("electronics");
        when(categoryRepository.findBySlug("electronics")).thenReturn(Mono.just(category));

        StepVerifier.create(service.getBySlug("electronics"))
                .expectNextMatches(c -> "electronics".equals(c.getSlug()))
                .verifyComplete();
    }

    @Test
    void getChildren_returnsChildCategories() {
        when(categoryRepository.findByParentId("parent"))
                .thenReturn(Flux.just(buildCategory("c1"), buildCategory("c2")));

        StepVerifier.create(service.getChildren("parent"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void create_setsActiveAndTimestamp() {
        Category input = new Category();
        input.setName("New Cat");

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> {
                    Category c = inv.getArgument(0);
                    c.setId("c-new");
                    return Mono.just(c);
                });

        StepVerifier.create(service.create(input))
                .expectNextMatches(c -> c.isActive() && c.getCreatedAt() != null && "c-new".equals(c.getId()))
                .verifyComplete();

        verify(categoryRepository).save(any(Category.class));
    }



    private Category buildCategory(String id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Category " + id);
        c.setSlug("slug-" + id);
        c.setActive(true);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
