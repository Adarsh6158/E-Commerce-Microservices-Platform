package com.ecommerce.catalog_service.Repository;

import com.ecommerce.catalog_service.Domain.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findBySku(String sku);

    Flux<Product> findByCategoryIdAndActiveTrue(String categoryId);
    
    Flux<Product> findByBrandAndActiveTrue(String brand);

    Flux<Product> findByActiveTrue();

    Mono<Boolean> existsBySku(String sku);
}
