package com.ecommerce.search_service.Service;

import com.ecommerce.search_service.Domain.ProductDocument;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

public interface SearchService {

    Flux<ProductDocument> search(String query, int page, int size);

    Flux<ProductDocument> searchByBrand(String brand, int page, int size);

    Flux<ProductDocument> searchByCategory(String categoryId, int page, int size);

    Flux<ProductDocument> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    Flux<ProductDocument> filterProducts(String q, String brand, String categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, int size);

    Flux<ProductDocument> suggest(String prefix);

    Flux<ProductDocument> getRecommendations(int size);
}
