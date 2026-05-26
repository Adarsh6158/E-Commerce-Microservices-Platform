package com.ecommerce.catalog_service.Service.Impl;

import com.ecommerce.catalog_service.Config.CatalogProperties;
import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.CreateReviewRequest;
import com.ecommerce.catalog_service.Dto.Response.ProductRatingResponse;
import com.ecommerce.catalog_service.Exception.DuplicateReviewException;
import com.ecommerce.catalog_service.Exception.ReviewNotFoundException;
import com.ecommerce.catalog_service.Repository.ReviewRepository;
import com.ecommerce.catalog_service.Service.ReviewOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewOperations {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final WebClient webClient;
    private final CatalogProperties properties;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             WebClient.Builder webClientBuilder,
                             CatalogProperties properties) {
        this.reviewRepository = reviewRepository;
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    @Override
    public Mono<Review> createReview(String productId,
                                     String userId,
                                     String userName,
                                     CreateReviewRequest request) {

        return reviewRepository.findByProductIdAndUserId(productId, userId)
                .flatMap(existing ->
                        Mono.<Review>error(new DuplicateReviewException(productId, userId))
                )
                .switchIfEmpty(
                        Mono.defer(() -> verifyPurchase(userId, productId))
                                .flatMap(purchased -> {
                                    Review review = new Review();
                                    review.setProductId(productId);
                                    review.setUserId(userId);
                                    review.setUserName(userName);
                                    review.setRating(request.rating());
                                    review.setTitle(request.title());
                                    review.setComment(request.comment());
                                    review.setVerified(purchased);
                                    review.setCreatedAt(Instant.now());

                                    return reviewRepository.save(review);
                                })
                )
                .doOnSuccess(r ->
                        log.info("Review created: productId={}, userId={}, verified={}",
                                productId, userId, r != null && r.isVerified())
                );
    }

    @Override
    public Flux<Review> getReviewsByProduct(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public Mono<ProductRatingResponse> getProductRating(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .collectList()
                .map(reviews -> {
                    if (reviews.isEmpty()) {
                        return new ProductRatingResponse(productId, 0.0, 0L);
                    }

                    double avg = reviews.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0.0);

                    return new ProductRatingResponse(
                            productId,
                            Math.round(avg * 10.0) / 10.0,
                            (long) reviews.size()
                    );
                });
    }

    @Override
    public Mono<Void> deleteReview(String reviewId, String userId) {
        return reviewRepository.findById(reviewId)
                .filter(r -> r.getUserId().equals(userId))
                .switchIfEmpty(
                        Mono.error(new ReviewNotFoundException(reviewId))
                )
                .flatMap(reviewRepository::delete);
    }

    private Mono<Boolean> verifyPurchase(String userId, String productId) {
        String host = properties.getOrderService().getHost();
        int port = properties.getOrderService().getPort();

        return webClient.get()
                .uri("http://{host}:{port}/orders/verify-purchase?userId={userId}&productId={productId}",
                        host, port, userId, productId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> Boolean.TRUE.equals(response.get("purchased")))
                .onErrorReturn(false)
                .doOnNext(result ->
                        log.debug("Purchase verification: userId={}, productId={}, purchased={}",
                                userId, productId, result)
                );
    }
}
