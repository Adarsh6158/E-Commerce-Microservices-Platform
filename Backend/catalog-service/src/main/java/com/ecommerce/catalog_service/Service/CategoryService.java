package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Domain.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

    Flux<Category> getAllActive();

    Mono<Category> getById(String id);

    Mono<Category> getBySlug(String slug);

    Flux<Category> getChildren(String parentId);

    Mono<Category> create(Category category);
}
