package com.ecommerce.catalog_service.Service.Impl;

import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Repository.CategoryRepository;
import com.ecommerce.catalog_service.Service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Flux<Category> getAllActive() {
        return categoryRepository.findByActiveTrue();
    }

    @Override
    public Mono<Category> getById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> getBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    public Flux<Category> getChildren(String parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public Mono<Category> create(Category category) {
        category.setActive(true);
        category.setCreatedAt(Instant.now());
        log.info("Creating category name={}", category.getName());
        return categoryRepository.save(category);
    }
}
