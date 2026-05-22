package com.ecommerce.search_service.Service;

import com.ecommerce.search_service.Config.SearchProperties;
import com.ecommerce.search_service.Domain.ProductDocument;
import com.ecommerce.search_service.Repository.ProductSearchRepository;
import com.ecommerce.search_service.Service.Impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

        @Mock
        private ProductSearchRepository searchRepository;

        @Mock
        private ElasticsearchOperations elasticsearchOperations;

        private SearchProperties searchProperties;
        private SearchServiceImpl searchService;

        @BeforeEach
        void setUp() {
                searchProperties = new SearchProperties();
                searchProperties.setSuggestLimit(8);
                searchProperties.setMaxPageSize(100);
                searchProperties.setMaxRecommendations(20);
                searchService = new SearchServiceImpl(searchRepository, elasticsearchOperations, searchProperties);
        }

        private ProductDocument createProduct(String id, String name, String brand) {
                ProductDocument doc = new ProductDocument();
                doc.setId(id);
                doc.setName(name);
                doc.setBrand(brand);
                doc.setActive(true);
                doc.setBasePrice(BigDecimal.valueOf(99.99));
                return doc;
        }

        @Nested
        @DisplayName("search()")
        class SearchTests {

                @Test
                @DisplayName("should return matching products for valid query")
                void searchReturnsResults() {
                        List<ProductDocument> products = List.of(
                                        createProduct("1", "Wireless Headphones", "Sony"),
                                        createProduct("2", "Wireless Earbuds", "Apple"));
                        when(searchRepository.findByNameContainingOrDescriptionContaining(
                                        eq("wireless"), eq("wireless"), any(PageRequest.class)))
                                        .thenReturn(products);

                        StepVerifier.create(searchService.search("wireless", 0, 20))
                                        .expectNextCount(2)
                                        .verifyComplete();

                        verify(searchRepository).findByNameContainingOrDescriptionContaining(
                                        "wireless", "wireless", PageRequest.of(0, 20));
                }

                @Test
                @DisplayName("should return empty flux when no results found")
                void searchReturnsEmpty() {
                        when(searchRepository.findByNameContainingOrDescriptionContaining(
                                        eq("nonexistent"), eq("nonexistent"), any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.search("nonexistent", 0, 20))
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should pass correct pagination parameters")
                void searchWithPagination() {
                        when(searchRepository.findByNameContainingOrDescriptionContaining(
                                        any(), any(), any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.search("test", 2, 50))
                                        .verifyComplete();

                        verify(searchRepository).findByNameContainingOrDescriptionContaining(
                                        "test", "test", PageRequest.of(2, 50));
                }

                @Test
                @DisplayName("should propagate repository exceptions")
                void searchHandlesRepositoryError() {
                        when(searchRepository.findByNameContainingOrDescriptionContaining(
                                        any(), any(), any(PageRequest.class)))
                                        .thenThrow(new RuntimeException("Elasticsearch connection failed"));

                        StepVerifier.create(searchService.search("test", 0, 20))
                                        .expectError(RuntimeException.class)
                                        .verify();
                }
        }

        @Nested
        @DisplayName("searchByBrand()")
        class SearchByBrandTests {

                @Test
                @DisplayName("should return products for valid brand")
                void searchByBrandReturnsResults() {
                        List<ProductDocument> products = List.of(createProduct("1", "Air Max", "Nike"));
                        when(searchRepository.findByBrandAndActiveTrue(eq("Nike"), any(PageRequest.class)))
                                        .thenReturn(products);

                        StepVerifier.create(searchService.searchByBrand("Nike", 0, 20))
                                        .expectNextCount(1)
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should return empty for unknown brand")
                void searchByBrandReturnsEmpty() {
                        when(searchRepository.findByBrandAndActiveTrue(eq("Unknown"), any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.searchByBrand("Unknown", 0, 20))
                                        .verifyComplete();
                }
        }

        @Nested
        @DisplayName("searchByCategory()")
        class SearchByCategoryTests {

                @Test
                @DisplayName("should return products for valid category")
                void searchByCategoryReturnsResults() {
                        List<ProductDocument> products = List.of(createProduct("1", "Laptop", "Dell"));
                        when(searchRepository.findByCategoryIdAndActiveTrue(eq("cat-1"), any(PageRequest.class)))
                                        .thenReturn(products);

                        StepVerifier.create(searchService.searchByCategory("cat-1", 0, 20))
                                        .expectNextCount(1)
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should return empty for unknown category")
                void searchByCategoryReturnsEmpty() {
                        when(searchRepository.findByCategoryIdAndActiveTrue(eq("cat-999"), any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.searchByCategory("cat-999", 0, 20))
                                        .verifyComplete();
                }
        }

        @Nested
        @DisplayName("searchByPriceRange()")
        class SearchByPriceRangeTests {

                @Test
                @DisplayName("should return products within price range")
                void searchByPriceRangeReturnsResults() {
                        List<ProductDocument> products = List.of(createProduct("1", "Budget Phone", "Samsung"));
                        when(searchRepository.findByBasePriceBetweenAndActiveTrue(
                                        eq(BigDecimal.valueOf(100)), eq(BigDecimal.valueOf(500)),
                                        any(PageRequest.class)))
                                        .thenReturn(products);

                        StepVerifier.create(searchService.searchByPriceRange(
                                        BigDecimal.valueOf(100), BigDecimal.valueOf(500), 0, 20))
                                        .expectNextCount(1)
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should return empty when no products in price range")
                void searchByPriceRangeReturnsEmpty() {
                        when(searchRepository.findByBasePriceBetweenAndActiveTrue(
                                        any(), any(), any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.searchByPriceRange(
                                        BigDecimal.valueOf(10000), BigDecimal.valueOf(20000), 0, 20))
                                        .verifyComplete();
                }
        }

        @Nested
        @DisplayName("filterProducts()")
        class FilterProductsTests {

                @SuppressWarnings("unchecked")
                @Test
                @DisplayName("should filter products successfully with all parameters")
                void filterWithAllParams() {
                        ProductDocument doc = createProduct("1", "Dell Laptop", "Dell");
                        SearchHits<ProductDocument> mockSearchHits = mock(SearchHits.class);
                        SearchHit<ProductDocument> mockHit = mock(SearchHit.class);
                        when(mockHit.getContent()).thenReturn(doc);
                        when(mockSearchHits.getSearchHits()).thenReturn(List.of(mockHit));
                        when(elasticsearchOperations.search(
                                        any(org.springframework.data.elasticsearch.core.query.Query.class),
                                        eq(ProductDocument.class)))
                                        .thenReturn((SearchHits<ProductDocument>) mockSearchHits);

                        StepVerifier.create(searchService.filterProducts(
                                        "laptop", "Dell", "cat-1",
                                        BigDecimal.valueOf(500), BigDecimal.valueOf(2000), 0, 20))
                                        .expectNext(doc)
                                        .verifyComplete();

                        verify(elasticsearchOperations).search(
                                        any(org.springframework.data.elasticsearch.core.query.Query.class),
                                        eq(ProductDocument.class));
                }

                @SuppressWarnings("unchecked")
                @Test
                @DisplayName("should filter products successfully with only query parameter")
                void filterWithQueryOnly() {
                        ProductDocument doc = createProduct("2", "Macbook", "Apple");
                        SearchHits<ProductDocument> mockSearchHits = mock(SearchHits.class);
                        SearchHit<ProductDocument> mockHit = mock(SearchHit.class);
                        when(mockHit.getContent()).thenReturn(doc);
                        when(mockSearchHits.getSearchHits()).thenReturn(List.of(mockHit));
                        when(elasticsearchOperations.search(
                                        any(org.springframework.data.elasticsearch.core.query.Query.class),
                                        eq(ProductDocument.class)))
                                        .thenReturn((SearchHits<ProductDocument>) mockSearchHits);

                        StepVerifier.create(searchService.filterProducts(
                                        "macbook", null, null,
                                        null, null, 0, 20))
                                        .expectNext(doc)
                                        .verifyComplete();
                }

                @SuppressWarnings("unchecked")
                @Test
                @DisplayName("should filter products successfully with only minPrice parameter")
                void filterWithMinPriceOnly() {
                        ProductDocument doc = createProduct("3", "Expensive", "Brand");
                        SearchHits<ProductDocument> mockSearchHits = mock(SearchHits.class);
                        SearchHit<ProductDocument> mockHit = mock(SearchHit.class);
                        when(mockHit.getContent()).thenReturn(doc);
                        when(mockSearchHits.getSearchHits()).thenReturn(List.of(mockHit));
                        when(elasticsearchOperations.search(
                                        any(org.springframework.data.elasticsearch.core.query.Query.class),
                                        eq(ProductDocument.class)))
                                        .thenReturn((SearchHits<ProductDocument>) mockSearchHits);

                        StepVerifier.create(searchService.filterProducts(
                                        null, null, null,
                                        BigDecimal.valueOf(100), null, 0, 20))
                                        .expectNext(doc)
                                        .verifyComplete();
                }

                @SuppressWarnings("unchecked")
                @Test
                @DisplayName("should return empty when no products match filter")
                void filterReturnsEmpty() {
                        SearchHits<ProductDocument> mockSearchHits = mock(SearchHits.class);
                        when(mockSearchHits.getSearchHits()).thenReturn(Collections.emptyList());
                        when(elasticsearchOperations.search(
                                        any(org.springframework.data.elasticsearch.core.query.Query.class),
                                        eq(ProductDocument.class)))
                                        .thenReturn((SearchHits<ProductDocument>) mockSearchHits);

                        StepVerifier.create(searchService.filterProducts(
                                        "nonexistent", null, null,
                                        null, null, 0, 20))
                                        .verifyComplete();
                }
        }

        @Nested
        @DisplayName("suggest()")
        class SuggestTests {

                @Test
                @DisplayName("should return empty for null prefix")
                void suggestReturnsEmptyForNull() {
                        StepVerifier.create(searchService.suggest(null))
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should return empty for blank prefix")
                void suggestReturnsEmptyForBlank() {
                        StepVerifier.create(searchService.suggest("   "))
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should return empty for empty string prefix")
                void suggestReturnsEmptyForEmptyString() {
                        StepVerifier.create(searchService.suggest(""))
                                        .verifyComplete();
                }
        }

        @Nested
        @DisplayName("getRecommendations()")
        class RecommendationsTests {

                @Test
                @DisplayName("should return active products as recommendations")
                void getRecommendationsReturnsResults() {
                        List<ProductDocument> products = List.of(
                                        createProduct("1", "Product A", "BrandA"),
                                        createProduct("2", "Product B", "BrandB"));
                        when(searchRepository.findByActiveTrue(any(PageRequest.class)))
                                        .thenReturn(products);

                        StepVerifier.create(searchService.getRecommendations(8))
                                        .expectNextCount(2)
                                        .verifyComplete();

                        verify(searchRepository).findByActiveTrue(PageRequest.of(0, 8));
                }

                @Test
                @DisplayName("should return empty when no active products")
                void getRecommendationsReturnsEmpty() {
                        when(searchRepository.findByActiveTrue(any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.getRecommendations(8))
                                        .verifyComplete();
                }

                @Test
                @DisplayName("should respect size parameter")
                void getRecommendationsRespectsSize() {
                        when(searchRepository.findByActiveTrue(any(PageRequest.class)))
                                        .thenReturn(Collections.emptyList());

                        StepVerifier.create(searchService.getRecommendations(5))
                                        .verifyComplete();

                        verify(searchRepository).findByActiveTrue(PageRequest.of(0, 5));
                }
        }
}
