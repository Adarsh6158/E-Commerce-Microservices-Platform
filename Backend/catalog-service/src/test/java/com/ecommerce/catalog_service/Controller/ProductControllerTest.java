package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Mapper.CatalogMapperImpl;
import com.ecommerce.catalog_service.Service.ProductService;
import com.ecommerce.catalog_service.Validator.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private CatalogMapper catalogMapper;
    private ProductValidator productValidator;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        catalogMapper = new CatalogMapperImpl();
        productValidator = new ProductValidator();
        controller = new ProductController(productService, catalogMapper, productValidator);
    }

    @Test
    void getById_returnsProductDto() {
        when(productService.getById("p1")).thenReturn(Mono.just(buildProduct("p1")));

        StepVerifier.create(controller.getById("p1"))
                .expectNextMatches(dto -> "p1".equals(dto.id()) && "Test".equals(dto.name()))
                .verifyComplete();
    }

    @Test
    void getBySku_returnsProductDto() {
        Product p = buildProduct("p1");
        when(productService.getBySku("SKU-001")).thenReturn(Mono.just(p));

        StepVerifier.create(controller.getBySku("SKU-001"))
                .expectNextMatches(dto -> "SKU-001".equals(dto.sku()))
                .verifyComplete();
    }

    @Test
    void getAll_returnsMultipleProducts() {
        when(productService.getAllActive())
                .thenReturn(Flux.just(buildProduct("p1"), buildProduct("p2")));

        StepVerifier.create(controller.getAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getBrands_returnsDistinctBrands() {
        when(productService.getDistinctBrands()).thenReturn(Flux.just("BrandA", "BrandB"));

        StepVerifier.create(controller.getBrands())
                .expectNextMatches(list -> list.size() == 2 && list.contains("BrandA"))
                .verifyComplete();
    }

    @Test
    void getByCategory_returnsProducts() {
        when(productService.getByCategory("cat1"))
                .thenReturn(Flux.just(buildProduct("p1")));

        StepVerifier.create(controller.getByCategory("cat1"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByBrand_returnsProducts() {
        when(productService.getByBrand("BrandX"))
                .thenReturn(Flux.just(buildProduct("p1")));

        StepVerifier.create(controller.getByBrand("BrandX"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void delete_completesSuccessfully() {
        when(productService.delete("p1", "corr-1")).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete("p1", "corr-1"))
                .verifyComplete();
    }

    private Product buildProduct(String id) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU-001");
        p.setName("Test");
        p.setBrand("BrandX");
        p.setBasePrice(new BigDecimal("19.99"));
        p.setActive(true);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }
}
