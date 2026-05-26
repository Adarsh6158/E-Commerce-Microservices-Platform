package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Config.CatalogProperties;
import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Event.ProductEvent;
import com.ecommerce.catalog_service.Event.ProductEventPublisher;
import com.ecommerce.catalog_service.Exception.ProductNotFoundException;
import com.ecommerce.catalog_service.Repository.ProductRepository;
import com.ecommerce.catalog_service.Service.Impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ReactiveStringRedisTemplate redisTemplate;
    @Mock private ReactiveValueOperations<String, String> valueOps;
    @Mock private ProductEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private CatalogProperties properties;
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        properties = new CatalogProperties();
        properties.getCache().setPrefix("product:");
        properties.getCache().setTtlMinutes(5);

        service = new ProductServiceImpl(productRepository, redisTemplate,
                eventPublisher, objectMapper, properties);
    }

    @Test
    void getById_cacheHit_returnsFromCache() throws Exception {
        Product product = buildProduct("p1");
        String json = objectMapper.writeValueAsString(product);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("product:p1")).thenReturn(Mono.just(json));


        lenient().when(productRepository.findById("p1")).thenReturn(Mono.empty());

        StepVerifier.create(service.getById("p1"))
                .expectNextMatches(p -> "p1".equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void getById_cacheMiss_fetchesFromDbAndCaches() throws Exception {
        Product product = buildProduct("p1");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("product:p1")).thenReturn(Mono.empty());
        when(productRepository.findById("p1")).thenReturn(Mono.just(product));
        when(valueOps.set(eq("product:p1"), anyString(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(service.getById("p1"))
                .expectNextMatches(p -> "p1".equals(p.getId()))
                .verifyComplete();

        verify(productRepository).findById("p1");
        verify(valueOps).set(eq("product:p1"), anyString(), any());
    }

    @Test
    void getBySku_delegatesToRepository() {
        Product product = buildProduct("p1");
        when(productRepository.findBySku("SKU-001")).thenReturn(Mono.just(product));

        StepVerifier.create(service.getBySku("SKU-001"))
                .expectNextMatches(p -> "SKU-001".equals(p.getSku()))
                .verifyComplete();
    }

    @Test
    void getAllActive_returnsActiveProducts() {
        when(productRepository.findByActiveTrue())
                .thenReturn(Flux.just(buildProduct("p1"), buildProduct("p2")));

        StepVerifier.create(service.getAllActive())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getDistinctBrands_filtersNullAndEmpty() {
        Product p1 = buildProduct("p1");
        p1.setBrand("BrandA");
        Product p2 = buildProduct("p2");
        p2.setBrand("BrandB");
        Product p3 = buildProduct("p3");
        p3.setBrand("BrandA");

        when(productRepository.findByActiveTrue()).thenReturn(Flux.just(p1, p2, p3));

        StepVerifier.create(service.getDistinctBrands())
                .expectNext("BrandA")
                .expectNext("BrandB")
                .verifyComplete();
    }

    @Test
    void create_savesAndPublishesEvent() {
        Product product = buildProduct(null);
        Product saved = buildProduct("p1");

        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(saved));
        doNothing().when(eventPublisher).publish(any(ProductEvent.class));

        StepVerifier.create(service.create(product, "corr-1"))
                .expectNextMatches(p -> "p1".equals(p.getId()))
                .verifyComplete();

        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publish(any(ProductEvent.class));
    }

    @Test
    void update_existingProduct_updatesAndClearsCache() {
        Product existing = buildProduct("p1");
        Product updates = new Product();
        updates.setName("Updated");
        updates.setActive(true);

        when(productRepository.findById("p1")).thenReturn(Mono.just(existing));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(existing));
        when(redisTemplate.delete("product:p1")).thenReturn(Mono.just(1L));
        doNothing().when(eventPublisher).publish(any(ProductEvent.class));

        StepVerifier.create(service.update("p1", updates, "corr-1"))
                .expectNextCount(1)
                .verifyComplete();

        verify(redisTemplate).delete("product:p1");
        verify(eventPublisher).publish(any(ProductEvent.class));
    }

    @Test
    void update_notFound_throwsProductNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.update("missing", new Product(), "corr-1"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void delete_deletesAndClearsCache() {
        when(productRepository.deleteById("p1")).thenReturn(Mono.empty());
        when(redisTemplate.delete("product:p1")).thenReturn(Mono.just(1L));
        doNothing().when(eventPublisher).publish(any(ProductEvent.class));

        StepVerifier.create(service.delete("p1", "corr-1"))
                .verifyComplete();

        verify(productRepository).deleteById("p1");
        verify(redisTemplate).delete("product:p1");
        verify(eventPublisher).publish(any(ProductEvent.class));
    }



    private Product buildProduct(String id) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU-001");
        p.setName("Test Product");
        p.setDescription("Desc");
        p.setCategoryId("cat1");
        p.setBrand("BrandX");
        p.setBasePrice(new BigDecimal("29.99"));
        p.setActive(true);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }
}
