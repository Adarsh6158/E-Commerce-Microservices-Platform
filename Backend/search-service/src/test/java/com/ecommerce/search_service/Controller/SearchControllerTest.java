package com.ecommerce.search_service.Controller;

import com.ecommerce.search_service.Config.SearchProperties;
import com.ecommerce.search_service.Domain.ProductDocument;
import com.ecommerce.search_service.Dto.Response.ProductSearchResult;
import com.ecommerce.search_service.Dto.Response.SuggestionResult;
import com.ecommerce.search_service.Exception.GlobalExceptionHandler;
import com.ecommerce.search_service.Mapper.ProductSearchMapper;
import com.ecommerce.search_service.Mapper.ProductSearchMapperImpl;
import com.ecommerce.search_service.Service.IndexingService;
import com.ecommerce.search_service.Service.SearchService;
import com.ecommerce.search_service.Validator.SearchRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private IndexingService indexingService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        SearchProperties searchProperties = new SearchProperties();
        searchProperties.setMaxPageSize(100);
        searchProperties.setMaxRecommendations(20);

        ProductSearchMapper mapper = new ProductSearchMapperImpl();
        SearchRequestValidator validator = new SearchRequestValidator(searchProperties);
        SearchController controller = new SearchController(searchService, indexingService, mapper, validator);

        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductDocument createProduct(String id, String name, String brand, BigDecimal price) {
        ProductDocument doc = new ProductDocument();
        doc.setId(id);
        doc.setName(name);
        doc.setBrand(brand);
        doc.setBasePrice(price);
        doc.setActive(true);
        doc.setCategoryId("cat-1");
        doc.setCategoryName("Electronics");
        doc.setSku("SKU-" + id);
        doc.setDescription("Description for " + name);
        doc.setUpdatedAt(Instant.parse("2024-01-15T10:30:00Z"));
        return doc;
    }

    @Nested
    @DisplayName("GET /search/products")
    class SearchProductsTests {

        @Test
        @DisplayName("should return search results for valid query")
        void searchReturnsResults() {
            when(searchService.search(eq("wireless"), eq(0), eq(20)))
                    .thenReturn(Flux.just(
                            createProduct("1", "Wireless Headphones", "Sony", BigDecimal.valueOf(299.99)),
                            createProduct("2", "Wireless Earbuds", "Apple", BigDecimal.valueOf(249.99))
                    ));

            webTestClient.get()
                    .uri("/search/products?q=wireless")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(2);
        }

        @Test
        @DisplayName("should return empty list for no matches")
        void searchReturnsEmpty() {
            when(searchService.search(eq("nonexistent"), eq(0), eq(20)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products?q=nonexistent")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(0);
        }

        @Test
        @DisplayName("should respect pagination parameters")
        void searchWithPagination() {
            when(searchService.search(eq("test"), eq(2), eq(10)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products?q=test&page=2&size=10")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should clamp oversized page size to max")
        void searchClampsSize() {
            when(searchService.search(eq("test"), eq(0), eq(100)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products?q=test&size=500")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should return error for missing query parameter")
        void searchMissingQuery() {
            webTestClient.get()
                    .uri("/search/products")
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("GET /search/products/brand/{brand}")
    class SearchByBrandTests {

        @Test
        @DisplayName("should return products for valid brand")
        void searchByBrand() {
            when(searchService.searchByBrand(eq("Nike"), eq(0), eq(20)))
                    .thenReturn(Flux.just(createProduct("1", "Air Max", "Nike", BigDecimal.valueOf(179.99))));

            webTestClient.get()
                    .uri("/search/products/brand/Nike")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("should return empty for unknown brand")
        void searchByUnknownBrand() {
            when(searchService.searchByBrand(eq("UnknownBrand"), eq(0), eq(20)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products/brand/UnknownBrand")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(0);
        }
    }

    @Nested
    @DisplayName("GET /search/products/category/{categoryId}")
    class SearchByCategoryTests {

        @Test
        @DisplayName("should return products for valid category")
        void searchByCategory() {
            when(searchService.searchByCategory(eq("cat-1"), eq(0), eq(20)))
                    .thenReturn(Flux.just(createProduct("1", "Laptop", "Dell", BigDecimal.valueOf(999.99))));

            webTestClient.get()
                    .uri("/search/products/category/cat-1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("GET /search/products/price")
    class SearchByPriceRangeTests {

        @Test
        @DisplayName("should return products within price range")
        void searchByPriceRange() {
            when(searchService.searchByPriceRange(
                    eq(BigDecimal.valueOf(100)), eq(BigDecimal.valueOf(500)), eq(0), eq(20)))
                    .thenReturn(Flux.just(createProduct("1", "Mid-range Phone", "Samsung", BigDecimal.valueOf(399.99))));

            webTestClient.get()
                    .uri("/search/products/price?min=100&max=500")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("should return 400 for invalid price range (min > max)")
        void invalidPriceRange() {
            webTestClient.get()
                    .uri("/search/products/price?min=500&max=100")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("should return 400 for negative min price")
        void negativePriceRange() {
            webTestClient.get()
                    .uri("/search/products/price?min=-10&max=100")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    @DisplayName("GET /search/products/filter")
    class FilterProductsTests {

        @Test
        @DisplayName("should filter with all parameters")
        void filterWithAllParams() {
            when(searchService.filterProducts(
                    eq("laptop"), eq("Dell"), eq("cat-1"),
                    eq(BigDecimal.valueOf(500)), eq(BigDecimal.valueOf(2000)),
                    eq(0), eq(20)))
                    .thenReturn(Flux.just(createProduct("1", "Dell Laptop", "Dell", BigDecimal.valueOf(999.99))));

            webTestClient.get()
                    .uri("/search/products/filter?q=laptop&brand=Dell&categoryId=cat-1&minPrice=500&maxPrice=2000")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("should filter with only query parameter")
        void filterWithQueryOnly() {
            when(searchService.filterProducts(
                    eq("laptop"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products/filter?q=laptop")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should filter with no parameters (returns all active)")
        void filterWithNoParams() {
            when(searchService.filterProducts(
                    isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/products/filter")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("GET /search/suggest")
    class SuggestTests {

        @Test
        @DisplayName("should return suggestions for valid prefix")
        void suggestReturnsResults() {
            ProductDocument doc = createProduct("1", "Wireless Headphones", "Sony", BigDecimal.valueOf(299.99));
            when(searchService.suggest(eq("wire")))
                    .thenReturn(Flux.just(doc));

            webTestClient.get()
                    .uri("/search/suggest?q=wire")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(SuggestionResult.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("should return empty for empty prefix")
        void suggestEmptyPrefix() {
            webTestClient.get()
                    .uri("/search/suggest?q=")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(SuggestionResult.class)
                    .hasSize(0);
        }

        @Test
        @DisplayName("should return empty when service errors")
        void suggestHandlesError() {
            when(searchService.suggest(eq("error")))
                    .thenReturn(Flux.error(new RuntimeException("ES down")));

            webTestClient.get()
                    .uri("/search/suggest?q=error")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(SuggestionResult.class)
                    .hasSize(0);
        }
    }

    @Nested
    @DisplayName("GET /search/recommendations")
    class RecommendationsTests {

        @Test
        @DisplayName("should return recommendations with default size")
        void recommendationsDefault() {
            when(searchService.getRecommendations(eq(8)))
                    .thenReturn(Flux.just(
                            createProduct("1", "Product A", "BrandA", BigDecimal.valueOf(49.99)),
                            createProduct("2", "Product B", "BrandB", BigDecimal.valueOf(79.99))
                    ));

            webTestClient.get()
                    .uri("/search/recommendations")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ProductSearchResult.class)
                    .hasSize(2);
        }

        @Test
        @DisplayName("should respect custom size parameter")
        void recommendationsCustomSize() {
            when(searchService.getRecommendations(eq(5)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/recommendations?size=5")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should clamp oversized recommendation request")
        void recommendationsClamped() {
            when(searchService.getRecommendations(eq(20)))
                    .thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/search/recommendations?size=100")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("POST /search/reindex")
    class ReindexTests {

        @Test
        @DisplayName("should trigger reindex and return count")
        void reindexSuccess() {
            when(indexingService.reindexAll()).thenReturn(Mono.just(150L));

            webTestClient.post()
                    .uri("/search/reindex")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("completed")
                    .jsonPath("$.indexed").isEqualTo(150);
        }

        @Test
        @DisplayName("should handle reindex with zero products")
        void reindexEmpty() {
            when(indexingService.reindexAll()).thenReturn(Mono.just(0L));

            webTestClient.post()
                    .uri("/search/reindex")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("completed")
                    .jsonPath("$.indexed").isEqualTo(0);
        }
    }
}
