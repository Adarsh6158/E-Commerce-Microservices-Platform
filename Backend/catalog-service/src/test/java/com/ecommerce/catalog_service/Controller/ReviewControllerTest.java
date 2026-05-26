package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.CreateReviewRequest;
import com.ecommerce.catalog_service.Dto.Response.ProductRatingResponse;
import com.ecommerce.catalog_service.Mapper.CatalogMapper;
import com.ecommerce.catalog_service.Mapper.CatalogMapperImpl;
import com.ecommerce.catalog_service.Service.ReviewOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock private ReviewOperations reviewOperations;

    private CatalogMapper catalogMapper;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        catalogMapper = new CatalogMapperImpl();
        controller = new ReviewController(reviewOperations, catalogMapper);
    }

    @Test
    void createReview_returnsReviewDto() {
        Review review = buildReview("r1");
        when(reviewOperations.createReview(eq("p1"), eq("u1"), eq("John"), any(CreateReviewRequest.class)))
                .thenReturn(Mono.just(review));

        CreateReviewRequest request = new CreateReviewRequest(5, "Great", "Nice product");

        StepVerifier.create(controller.createReview("p1", request, "u1", "John"))
                .expectNextMatches(dto -> "r1".equals(dto.id()) && dto.rating() == 5)
                .verifyComplete();
    }

    @Test
    void getReviews_returnsReviewDtos() {
        when(reviewOperations.getReviewsByProduct("p1"))
                .thenReturn(Flux.just(buildReview("r1"), buildReview("r2")));

        StepVerifier.create(controller.getReviews("p1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getRating_returnsProductRatingResponse() {
        ProductRatingResponse rating = new ProductRatingResponse("p1", 4.5, 10L);
        when(reviewOperations.getProductRating("p1")).thenReturn(Mono.just(rating));

        StepVerifier.create(controller.getRating("p1"))
                .expectNextMatches(r -> r.averageRating() == 4.5 && r.reviewCount() == 10L)
                .verifyComplete();
    }

    @Test
    void deleteReview_completesSuccessfully() {
        when(reviewOperations.deleteReview("r1", "u1")).thenReturn(Mono.empty());

        StepVerifier.create(controller.deleteReview("p1", "r1", "u1"))
                .verifyComplete();
    }



    private Review buildReview(String id) {
        Review r = new Review();
        r.setId(id);
        r.setProductId("p1");
        r.setUserId("u1");
        r.setUserName("John");
        r.setRating(5);
        r.setTitle("Great");
        r.setComment("Nice");
        r.setVerified(true);
        r.setCreatedAt(Instant.now());
        return r;
    }
}
