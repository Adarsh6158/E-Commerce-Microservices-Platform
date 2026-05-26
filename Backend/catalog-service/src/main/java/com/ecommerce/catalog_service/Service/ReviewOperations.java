package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.CreateReviewRequest;
import com.ecommerce.catalog_service.Dto.Response.ProductRatingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewOperations {

    Mono<Review> createReview(String productId, String userId, String userName, CreateReviewRequest request);

    Flux<Review> getReviewsByProduct(String productId);

    Mono<ProductRatingResponse> getProductRating(String productId);

    Mono<Void> deleteReview(String reviewId, String userId);
}
