package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Constant.AppConstants;
import com.ecommerce.catalog_service.Dto.Request.CreateReviewRequest;
import com.ecommerce.catalog_service.Dto.Response.ProductRatingResponse;
import com.ecommerce.catalog_service.Dto.Response.ReviewDto;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Service.ReviewOperations;
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
@RequestMapping("/products/{productId}/reviews")
@Tag(name = "Reviews", description = "Product review management")
public class ReviewController {

    private final ReviewOperations reviewOperations;
    private final CatalogMapper catalogMapper;

    public ReviewController(ReviewOperations reviewOperations, CatalogMapper catalogMapper) {
        this.reviewOperations = reviewOperations;
        this.catalogMapper = catalogMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create review", description = "Creates a product review. Verifies purchase via order-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate review")
    })
    public Mono<ReviewDto> createReview(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = AppConstants.DEFAULT_USER_ID) String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = AppConstants.DEFAULT_USER_NAME) String userName) {

        return reviewOperations.createReview(productId, userId, userName, request)
                .map(catalogMapper::toReviewDto);
    }

    @GetMapping
    @Operation(summary = "Get reviews", description = "Returns all reviews for a product, ordered by most recent")
    public Flux<ReviewDto> getReviews(@Parameter(description = "Product ID") @PathVariable String productId) {
        return reviewOperations.getReviewsByProduct(productId)
                .map(catalogMapper::toReviewDto);
    }

    @GetMapping("/rating")
    @Operation(summary = "Get product rating", description = "Returns the average rating and review count for a product")
    public Mono<ProductRatingResponse> getRating(@Parameter(description = "Product ID") @PathVariable String productId) {
        return reviewOperations.getProductRating(productId);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete review", description = "Deletes a review. Only the review author can delete.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted"),
            @ApiResponse(responseCode = "404", description = "Review not found or not authorized")
    })
    public Mono<Void> deleteReview(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Review ID") @PathVariable String reviewId,
            @RequestHeader(value = "X-User-Id", defaultValue = AppConstants.DEFAULT_USER_ID) String userId) {

        return reviewOperations.deleteReview(reviewId, userId);
    }
}
