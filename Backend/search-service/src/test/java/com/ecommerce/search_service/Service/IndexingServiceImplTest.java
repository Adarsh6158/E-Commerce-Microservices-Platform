package com.ecommerce.search_service.Service;

import com.ecommerce.search_service.Config.CatalogProperties;
import com.ecommerce.search_service.Domain.ProductDocument;
import com.ecommerce.search_service.Exception.IndexingException;
import com.ecommerce.search_service.Repository.ProductSearchRepository;
import com.ecommerce.search_service.Service.Impl.IndexingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexingServiceImplTest {

    @Mock
    private ProductSearchRepository searchRepository;

    @Mock
    private WebClient catalogWebClient;

    private CatalogProperties catalogProperties;
    private IndexingServiceImpl indexingService;

    @BeforeEach
    void setUp() {
        catalogProperties = new CatalogProperties();
        catalogProperties.setHost("localhost");
        catalogProperties.setPort(8082);
        catalogProperties.setProductsPath("/products");
        indexingService = new IndexingServiceImpl(searchRepository, catalogWebClient, catalogProperties);
    }

    private ProductDocument createProduct(String id, String name) {
        ProductDocument doc = new ProductDocument();
        doc.setId(id);
        doc.setName(name);
        doc.setActive(true);
        doc.setBasePrice(BigDecimal.valueOf(49.99));
        return doc;
    }

    @Nested
    @DisplayName("indexProduct()")
    class IndexProductTests {

        @Test
        @DisplayName("should index product successfully")
        void indexProductSuccess() {
            ProductDocument product = createProduct("prod-1", "Test Product");
            when(searchRepository.save(any(ProductDocument.class))).thenReturn(product);

            StepVerifier.create(indexingService.indexProduct(product))
                    .assertNext(doc -> {
                        assertThat(doc.getId()).isEqualTo("prod-1");
                        assertThat(doc.getName()).isEqualTo("Test Product");
                    })
                    .verifyComplete();

            verify(searchRepository).save(product);
        }

        @Test
        @DisplayName("should wrap repository exception as IndexingException")
        void indexProductFailure() {
            ProductDocument product = createProduct("prod-1", "Test Product");
            when(searchRepository.save(any(ProductDocument.class)))
                    .thenThrow(new RuntimeException("ES connection failed"));

            StepVerifier.create(indexingService.indexProduct(product))
                    .expectError(IndexingException.class)
                    .verify();
        }

        @Test
        @DisplayName("should index product with all fields populated")
        void indexProductWithAllFields() {
            ProductDocument product = createProduct("prod-1", "Full Product");
            product.setBrand("TestBrand");
            product.setCategoryId("cat-1");
            product.setCategoryName("Electronics");
            product.setSku("SKU-001");
            product.setDescription("A test product description");
            when(searchRepository.save(any(ProductDocument.class))).thenReturn(product);

            StepVerifier.create(indexingService.indexProduct(product))
                    .assertNext(doc -> {
                        assertThat(doc.getBrand()).isEqualTo("TestBrand");
                        assertThat(doc.getCategoryId()).isEqualTo("cat-1");
                        assertThat(doc.getSku()).isEqualTo("SKU-001");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should index product with null optional fields")
        void indexProductWithNullFields() {
            ProductDocument product = new ProductDocument();
            product.setId("prod-2");
            product.setName("Minimal Product");
            product.setActive(true);
            when(searchRepository.save(any(ProductDocument.class))).thenReturn(product);

            StepVerifier.create(indexingService.indexProduct(product))
                    .assertNext(doc -> {
                        assertThat(doc.getBrand()).isNull();
                        assertThat(doc.getBasePrice()).isNull();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProductTests {

        @Test
        @DisplayName("should delete product successfully")
        void deleteProductSuccess() {
            doNothing().when(searchRepository).deleteById("prod-1");

            StepVerifier.create(indexingService.deleteProduct("prod-1"))
                    .verifyComplete();

            verify(searchRepository).deleteById("prod-1");
        }

        @Test
        @DisplayName("should wrap repository exception on delete")
        void deleteProductFailure() {
            doThrow(new RuntimeException("Delete failed"))
                    .when(searchRepository).deleteById("prod-1");

            StepVerifier.create(indexingService.deleteProduct("prod-1"))
                    .expectError(IndexingException.class)
                    .verify();
        }

        @Test
        @DisplayName("should handle deleting non-existent product gracefully")
        void deleteNonExistentProduct() {
            doNothing().when(searchRepository).deleteById("non-existent");

            StepVerifier.create(indexingService.deleteProduct("non-existent"))
                    .verifyComplete();

            verify(searchRepository).deleteById("non-existent");
        }
    }

    @Nested
    @DisplayName("reindexAll()")
    class ReindexAllTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should reindex products successfully from catalog")
        void reindexAllSuccess() {
            WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(catalogWebClient).get();
            doReturn(headersSpec).when(uriSpec).uri(anyString());
            doReturn(responseSpec).when(headersSpec).retrieve();
            
            ProductDocument doc1 = createProduct("1", "Prod1");
            ProductDocument doc2 = createProduct("2", "Prod2");
            when(responseSpec.bodyToFlux(ProductDocument.class)).thenReturn(Flux.just(doc1, doc2));
            
            when(searchRepository.save(any(ProductDocument.class))).thenAnswer(i -> i.getArgument(0));

            StepVerifier.create(indexingService.reindexAll())
                    .expectNext(2L)
                    .verifyComplete();
                    
            verify(searchRepository).deleteAll();
            verify(searchRepository, times(2)).save(any(ProductDocument.class));
        }
        
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("should return 0 when catalog has no products")
        void reindexAllEmpty() {
            WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(catalogWebClient).get();
            doReturn(headersSpec).when(uriSpec).uri(anyString());
            doReturn(responseSpec).when(headersSpec).retrieve();
            
            when(responseSpec.bodyToFlux(ProductDocument.class)).thenReturn(Flux.empty());

            StepVerifier.create(indexingService.reindexAll())
                    .expectNext(0L)
                    .verifyComplete();
                    
            verify(searchRepository).deleteAll();
            verify(searchRepository, never()).save(any());
        }
    }
}
