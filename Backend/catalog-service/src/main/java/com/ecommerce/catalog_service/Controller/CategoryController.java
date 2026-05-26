package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Dto.Request.CreateCategoryRequest;
import com.ecommerce.catalog_service.Dto.Response.CategoryDto;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Service.CategoryService;
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
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Product category management")
public class CategoryController {

    private final CategoryService categoryService;
    private final CatalogMapper catalogMapper;

    public CategoryController(CategoryService categoryService, CatalogMapper catalogMapper) {
        this.categoryService = categoryService;
        this.catalogMapper = catalogMapper;
    }

    @GetMapping
    @Operation(summary = "Get all active categories", description = "Returns all categories with active=true")
    public Flux<CategoryDto> getAll() {
        return categoryService.getAllActive().map(catalogMapper::toCategoryDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public Mono<CategoryDto> getById(@Parameter(description = "Category ID") @PathVariable String id) {
        return categoryService.getById(id).map(catalogMapper::toCategoryDto);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a category by its URL-friendly slug")
    public Mono<CategoryDto> getBySlug(@Parameter(description = "Category slug") @PathVariable String slug) {
        return categoryService.getBySlug(slug).map(catalogMapper::toCategoryDto);
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Returns all direct children of a category")
    public Flux<CategoryDto> getChildren(@Parameter(description = "Parent category ID") @PathVariable String id) {
        return categoryService.getChildren(id).map(catalogMapper::toCategoryDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category", description = "Creates a new product category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public Mono<CategoryDto> create(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(catalogMapper.toEntity(request)).map(catalogMapper::toCategoryDto);
    }
}