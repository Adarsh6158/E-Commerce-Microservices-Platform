package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> getById(String id);

    Mono<Product> getBySku(String sku);

    Flux<Product> getByCategory(String categoryId);

    Flux<Product> getByBrand(String brand);

    Flux<Product> getAllActive();

    Flux<String> getDistinctBrands();

    Mono<Product> create(Product product, String correlationId);

    Mono<Product> update(String id, Product updates, String correlationId);

    Mono<Void> delete(String id, String correlationId);
}
