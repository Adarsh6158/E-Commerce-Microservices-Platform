package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Dto.CreateProductRequest;
import com.ecommerce.catalog_service.Dto.DtoMapper;
import com.ecommerce.catalog_service.Dto.ProductDto;
import com.ecommerce.catalog_service.Dto.UpdateProductRequest;
import com.ecommerce.catalog_service.Service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/{id}")
    public Mono<ProductDto> getById(@PathVariable String id) {
        return catalogService.getById(id).map(DtoMapper::toDto);
    }

    @GetMapping("/sku/{sku}")
    public Mono<ProductDto> getBySku(@PathVariable String sku) {
        return catalogService.getBySku(sku).map(DtoMapper::toDto);
    }

    @GetMapping
    public Flux<ProductDto> getAll() {
        return catalogService.getAllActive().map(DtoMapper::toDto);
    }

    @GetMapping("/brands")
    public Mono<java.util.List<String>> getBrands() {
        return catalogService.getDistinctBrands().collectList();
    }

    @GetMapping("/category/{categoryId}")
    public Flux<ProductDto> getByCategory(@PathVariable String categoryId) {
        return catalogService.getByCategory(categoryId).map(DtoMapper::toDto);
    }

    @GetMapping("/brand/{brand}")
    public Flux<ProductDto> getByBrand(@PathVariable String brand) {
        return catalogService.getByBrand(brand).map(DtoMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductDto> create(@Valid @RequestBody CreateProductRequest request,
                                   @RequestHeader(value = "X-Correlation-Id", defaultValue = "unknown") String correlationId) {
        return catalogService.create(DtoMapper.toEntity(request), correlationId)
                .map(DtoMapper::toDto);
    }

    @PutMapping("/{id}")
    public Mono<ProductDto> update(@PathVariable String id,
                                   @RequestBody UpdateProductRequest request,
                                   @RequestHeader(value = "X-Correlation-Id", defaultValue = "unknown") String correlationId) {

        var product = new com.ecommerce.catalog_service.Domain.Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
        product.setBrand(request.brand());
        product.setBasePrice(request.basePrice());
        product.setImage(request.image());
        product.setThumbnail(request.thumbnail());
        product.setGalleryImages(request.galleryImages());
        product.setAltText(request.altText());
        if (request.active() != null) product.setActive(request.active());
        product.setWeight(request.weight());
        product.setAttributes(request.attributes());

        return catalogService.update(id, product, correlationId)
                .map(DtoMapper::toDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id,
                             @RequestHeader(value = "X-Correlation-Id", defaultValue = "unknown") String correlationId) {
        return catalogService.delete(id, correlationId);
    }
}
