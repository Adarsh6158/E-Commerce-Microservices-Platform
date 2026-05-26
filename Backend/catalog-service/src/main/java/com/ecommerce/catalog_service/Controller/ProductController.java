package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Constant.AppConstants;
import com.ecommerce.catalog_service.Dto.Request.CreateProductRequest;
import com.ecommerce.catalog_service.Dto.Request.UpdateProductRequest;
import com.ecommerce.catalog_service.Dto.Response.ProductDto;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Service.ProductService;
import com.ecommerce.catalog_service.Validator.ProductValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;
    private final CatalogMapper catalogMapper;
    private final ProductValidator productValidator;

    public ProductController(ProductService productService,
                             CatalogMapper catalogMapper,
                             ProductValidator productValidator) {
        this.productService = productService;
        this.catalogMapper = catalogMapper;
        this.productValidator = productValidator;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its unique identifier, with Redis caching")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public Mono<ProductDto> getById(@Parameter(description = "Product ID") @PathVariable String id) {
        return productService.getById(id).map(catalogMapper::toProductDto);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves a product by its stock keeping unit code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public Mono<ProductDto> getBySku(@Parameter(description = "Product SKU") @PathVariable String sku) {
        return productService.getBySku(sku).map(catalogMapper::toProductDto);
    }

    @GetMapping
    @Operation(summary = "Get all active products", description = "Returns all products with active=true")
    public Flux<ProductDto> getAll() {
        return productService.getAllActive().map(catalogMapper::toProductDto);
    }

    @GetMapping("/brands")
    @Operation(summary = "Get distinct brands", description = "Returns a list of unique brand names from active products")
    public Mono<java.util.List<String>> getBrands() {
        return productService.getDistinctBrands().collectList();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Returns all active products in a given category")
    public Flux<ProductDto> getByCategory(@Parameter(description = "Category ID") @PathVariable String categoryId) {
        return productService.getByCategory(categoryId).map(catalogMapper::toProductDto);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Returns all active products of a given brand")
    public Flux<ProductDto> getByBrand(@Parameter(description = "Brand name") @PathVariable String brand) {
        return productService.getByBrand(brand).map(catalogMapper::toProductDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", description = "Creates a new product and publishes a Kafka event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public Mono<ProductDto> create(@Valid @RequestBody CreateProductRequest request,
                                   @Parameter(description = "Correlation ID for tracing")
                                   @RequestHeader(value = "X-Correlation-Id", defaultValue = AppConstants.DEFAULT_CORRELATION_ID) String correlationId) {
        productValidator.validateCreateRequest(request);
        return productService.create(catalogMapper.toEntity(request), correlationId)
                .map(catalogMapper::toProductDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product and publishes a Kafka event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public Mono<ProductDto> update(@Parameter(description = "Product ID") @PathVariable String id,
                                   @Valid @RequestBody UpdateProductRequest request,
                                   @Parameter(description = "Correlation ID for tracing")
                                   @RequestHeader(value = "X-Correlation-Id", defaultValue = AppConstants.DEFAULT_CORRELATION_ID) String correlationId) {

        productValidator.validateUpdateRequest(request);
        var product = catalogMapper.applyUpdates(new com.ecommerce.catalog_service.Domain.Product(), request);

        return productService.update(id, product, correlationId)
                .map(catalogMapper::toProductDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", description = "Deletes a product and publishes a Kafka event")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public Mono<Void> delete(@Parameter(description = "Product ID") @PathVariable String id,
                             @Parameter(description = "Correlation ID for tracing")
                             @RequestHeader(value = "X-Correlation-Id", defaultValue = AppConstants.DEFAULT_CORRELATION_ID) String correlationId) {
        return productService.delete(id, correlationId);
    }
}
