package com.ecommerce.search_service.Event;

import com.ecommerce.search_service.Domain.ProductDocument;
import com.ecommerce.search_service.Dto.Event.ProductEventPayload;
import com.ecommerce.search_service.Mapper.ProductSearchMapper;
import com.ecommerce.search_service.Service.IndexingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {

    @Mock
    private IndexingService indexingService;

    @Mock
    private ProductSearchMapper mapper;

    private ObjectMapper objectMapper;
    private ProductEventConsumer consumer;

    @Captor
    private ArgumentCaptor<ProductDocument> documentCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new ProductEventConsumer(indexingService, mapper, objectMapper);
    }

    @Nested
    @DisplayName("onProductUpsert()")
    class OnProductUpsertTests {

        @Test
        @DisplayName("should process valid product created event")
        void processCreatedEvent() {
            String payload = """
                    {
                        "eventType": "PRODUCT_CREATED",
                        "productId": "prod-1",
                        "name": "Test Product",
                        "brand": "TestBrand",
                        "basePrice": 99.99,
                        "active": true
                    }
                    """;

            ProductDocument doc = new ProductDocument();
            doc.setId("prod-1");
            when(mapper.toDocument(any(ProductEventPayload.class))).thenReturn(doc);
            when(indexingService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.just(doc));

            consumer.onProductUpsert(payload, "catalog.product.created");

            verify(mapper).toDocument(any(ProductEventPayload.class));
            verify(indexingService).indexProduct(doc);
        }

        @Test
        @DisplayName("should process valid product updated event")
        void processUpdatedEvent() {
            String payload = """
                    {
                        "eventType": "PRODUCT_UPDATED",
                        "productId": "prod-1",
                        "name": "Updated Product",
                        "brand": "NewBrand",
                        "basePrice": 149.99,
                        "active": true
                    }
                    """;

            ProductDocument doc = new ProductDocument();
            doc.setId("prod-1");
            when(mapper.toDocument(any(ProductEventPayload.class))).thenReturn(doc);
            when(indexingService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.just(doc));

            consumer.onProductUpsert(payload, "catalog.product.updated");

            verify(indexingService).indexProduct(doc);
        }

        @Test
        @DisplayName("should handle malformed JSON gracefully")
        void handleMalformedJson() {
            String payload = "not valid json {{{";

            consumer.onProductUpsert(payload, "catalog.product.created");

            verifyNoInteractions(indexingService);
        }

        @Test
        @DisplayName("should handle empty payload gracefully")
        void handleEmptyPayload() {
            consumer.onProductUpsert("", "catalog.product.created");

            verifyNoInteractions(indexingService);
        }

        @Test
        @DisplayName("should process event with missing optional fields")
        void processEventWithMissingFields() {
            String payload = """
                    {
                        "productId": "prod-2",
                        "name": "Minimal Product"
                    }
                    """;

            ProductDocument doc = new ProductDocument();
            doc.setId("prod-2");
            when(mapper.toDocument(any(ProductEventPayload.class))).thenReturn(doc);
            when(indexingService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.just(doc));

            consumer.onProductUpsert(payload, "catalog.product.created");

            verify(indexingService).indexProduct(doc);
        }

        @Test
        @DisplayName("should handle event with gallery images")
        void processEventWithGalleryImages() {
            String payload = """
                    {
                        "productId": "prod-3",
                        "name": "Gallery Product",
                        "galleryImages": ["img1.jpg", "img2.jpg", "img3.jpg"],
                        "active": true
                    }
                    """;

            ProductDocument doc = new ProductDocument();
            doc.setId("prod-3");
            when(mapper.toDocument(any(ProductEventPayload.class))).thenReturn(doc);
            when(indexingService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.just(doc));

            consumer.onProductUpsert(payload, "catalog.product.created");

            verify(mapper).toDocument(any(ProductEventPayload.class));
        }

        @Test
        @DisplayName("should handle unknown fields in payload without error")
        void processEventWithUnknownFields() {
            String payload = """
                    {
                        "productId": "prod-4",
                        "name": "Future Product",
                        "newField": "unknown",
                        "anotherNewField": 42
                    }
                    """;

            ProductDocument doc = new ProductDocument();
            doc.setId("prod-4");
            when(mapper.toDocument(any(ProductEventPayload.class))).thenReturn(doc);
            when(indexingService.indexProduct(any(ProductDocument.class))).thenReturn(Mono.just(doc));

            consumer.onProductUpsert(payload, "catalog.product.created");

            verify(indexingService).indexProduct(doc);
        }
    }

    @Nested
    @DisplayName("onProductDeleted()")
    class OnProductDeletedTests {

        @Test
        @DisplayName("should process valid delete event")
        void processDeleteEvent() {
            String payload = """
                    {
                        "eventType": "PRODUCT_DELETED",
                        "productId": "prod-1"
                    }
                    """;

            when(indexingService.deleteProduct("prod-1")).thenReturn(Mono.empty());

            consumer.onProductDeleted(payload, "catalog.product.deleted");

            verify(indexingService).deleteProduct("prod-1");
        }

        @Test
        @DisplayName("should handle malformed delete event gracefully")
        void handleMalformedDeleteEvent() {
            String payload = "invalid json";

            consumer.onProductDeleted(payload, "catalog.product.deleted");

            verifyNoInteractions(indexingService);
        }

        @Test
        @DisplayName("should handle empty delete payload gracefully")
        void handleEmptyDeletePayload() {
            consumer.onProductDeleted("", "catalog.product.deleted");

            verifyNoInteractions(indexingService);
        }
    }
}
