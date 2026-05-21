package com.ecommerce.search_service.Controller;

import com.ecommerce.search_service.Dto.Response.ProductSearchResult;
import com.ecommerce.search_service.Dto.Response.SuggestionResult;
import com.ecommerce.search_service.Mapper.ProductSearchMapper;
import com.ecommerce.search_service.Service.IndexingService;
import com.ecommerce.search_service.Service.SearchService;
import com.ecommerce.search_service.Validator.SearchRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static com.ecommerce.search_service.Constant.SearchConstants.*;

@RestController
@RequestMapping("/search")
@Tag(name = "Search", description = "Product search, filtering, suggestions, and recommendations")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;
    private final IndexingService indexingService;
    private final ProductSearchMapper mapper;
    private final SearchRequestValidator validator;

    public SearchController(SearchService searchService,
                            IndexingService indexingService,
                            ProductSearchMapper mapper,
                            SearchRequestValidator validator) {
        this.searchService = searchService;
        this.indexingService = indexingService;
        this.mapper = mapper;
        this.validator = validator;
    }

    @GetMapping("/products")
    @Operation(summary = "Search products", description = "Full-text search across product name and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Flux<ProductSearchResult> search(
            @Parameter(description = "Search query", example = "wireless headphones") @RequestParam String q,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        validator.validateQuery(q);
        validator.validatePage(page);
        int clampedSize = validator.validateAndClampSize(size);
        return searchService.search(q, page, clampedSize)
                .map(mapper::toSearchResult);
    }

    @GetMapping("/products/brand/{brand}")
    @Operation(summary = "Search by brand", description = "Find active products by brand name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products for the brand"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public Flux<ProductSearchResult> searchByBrand(
            @Parameter(description = "Brand name", example = "Nike") @PathVariable String brand,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        validator.validatePage(page);
        int clampedSize = validator.validateAndClampSize(size);
        return searchService.searchByBrand(brand, page, clampedSize)
                .map(mapper::toSearchResult);
    }

    @GetMapping("/products/category/{categoryId}")
    @Operation(summary = "Search by category", description = "Find active products by category ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products in the category"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public Flux<ProductSearchResult> searchByCategory(
            @Parameter(description = "Category ID") @PathVariable String categoryId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        validator.validatePage(page);
        int clampedSize = validator.validateAndClampSize(size);
        return searchService.searchByCategory(categoryId, page, clampedSize)
                .map(mapper::toSearchResult);
    }

    @GetMapping("/products/price")
    @Operation(summary = "Search by price range", description = "Find active products within a price range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products in the price range"),
            @ApiResponse(responseCode = "400", description = "Invalid price range")
    })
    public Flux<ProductSearchResult> searchByPriceRange(
            @Parameter(description = "Minimum price", example = "10.00") @RequestParam BigDecimal min,
            @Parameter(description = "Maximum price", example = "500.00") @RequestParam BigDecimal max,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        validator.validatePriceRange(min, max);
        validator.validatePage(page);
        int clampedSize = validator.validateAndClampSize(size);
        return searchService.searchByPriceRange(min, max, page, clampedSize)
                .map(mapper::toSearchResult);
    }

    @GetMapping("/products/filter")
    @Operation(summary = "Filter products", description = "Advanced filtering with multiple criteria using Elasticsearch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered results"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    public Flux<ProductSearchResult> filterProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Brand filter") @RequestParam(required = false) String brand,
            @Parameter(description = "Category ID filter") @RequestParam(required = false) String categoryId,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        validator.validatePriceRange(minPrice, maxPrice);
        validator.validatePage(page);
        int clampedSize = validator.validateAndClampSize(size);
        return searchService.filterProducts(q, brand, categoryId, minPrice, maxPrice, page, clampedSize)
                .map(mapper::toSearchResult);
    }

    @GetMapping("/suggest")
    @Operation(summary = "Search suggestions", description = "Auto-complete suggestions based on query prefix")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suggestion results")
    })
    public Flux<SuggestionResult> suggest(
            @Parameter(description = "Search prefix", example = "wire") @RequestParam String q) {
        if (q == null || q.trim().length() < 1) {
            return Flux.empty();
        }
        return searchService.suggest(q.trim())
                .map(mapper::toSuggestionResult)
                .onErrorResume(e -> {
                    log.error("Suggest failed for q='{}' | Error type: {} | Message: {}",
                            q, e.getClass().getSimpleName(), e.getMessage());
                    return Flux.empty();
                });
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Product recommendations", description = "Get recommended active products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommended products")
    })
    public Flux<ProductSearchResult> recommendations(
            @Parameter(description = "Number of recommendations (max 20)") @RequestParam(defaultValue = "8") int size) {
        int clampedSize = validator.clampRecommendationSize(size);
        return searchService.getRecommendations(clampedSize)
                .map(mapper::toSearchResult);
    }

    @PostMapping("/reindex")
    @Operation(summary = "Reindex all products", description = "Trigger full reindex from catalog service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reindex completed"),
            @ApiResponse(responseCode = "500", description = "Reindex failed")
    })
    public Mono<Map<String, Object>> reindex() {
        return indexingService.reindexAll()
                .map(count -> Map.of(
                        RESPONSE_STATUS, RESPONSE_COMPLETED,
                        RESPONSE_INDEXED, count
                ));
    }
}
