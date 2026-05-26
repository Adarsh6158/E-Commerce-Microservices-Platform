package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Config.CatalogProperties;
import com.ecommerce.catalog_service.Domain.Review;
import com.ecommerce.catalog_service.Dto.Request.CreateReviewRequest;
import com.ecommerce.catalog_service.Exception.DuplicateReviewException;
import com.ecommerce.catalog_service.Exception.ReviewNotFoundException;
import com.ecommerce.catalog_service.Repository.ReviewRepository;
import com.ecommerce.catalog_service.Service.Impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

        @Mock
        private ReviewRepository reviewRepository;

        private CatalogProperties properties;
        private ReviewServiceImpl service;

        @BeforeEach
        void setUp() {
                properties = new CatalogProperties();
                properties.getOrderService().setHost("localhost");
                properties.getOrderService().setPort(8087);

                WebClient.Builder webClientBuilder = WebClient.builder();
                service = new ReviewServiceImpl(reviewRepository, webClientBuilder, properties);
        }

        @Test
        void createReview_newReview_savesSuccessfully() {
                CreateReviewRequest request = new CreateReviewRequest(5, "Great!", "Loved it");

                when(reviewRepository.findByProductIdAndUserId("p1", "u1")).thenReturn(Mono.empty());
                when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
                        Review r = inv.getArgument(0);
                        r.setId("r1");
                        return Mono.just(r);
                });

                StepVerifier.create(service.createReview("p1", "u1", "John", request))
                                .expectNextMatches(r -> "r1".equals(r.getId()) && r.getRating() == 5)
                                .verifyComplete();

                verify(reviewRepository).save(any(Review.class));
        }

        @Test
        void createReview_duplicateReview_throwsDuplicateReviewException() {
                Review existing = buildReview("r1", "p1", "u1");
                when(reviewRepository.findByProductIdAndUserId("p1", "u1")).thenReturn(Mono.just(existing));

                CreateReviewRequest request = new CreateReviewRequest(3, "Another", "Review");

                StepVerifier.create(service.createReview("p1", "u1", "John", request))
                                .expectError(DuplicateReviewException.class)
                                .verify();

                verify(reviewRepository, never()).save(any());
        }

        @Test
        void getReviewsByProduct_returnsReviews() {
                when(reviewRepository.findByProductIdOrderByCreatedAtDesc("p1"))
                                .thenReturn(Flux.just(buildReview("r1", "p1", "u1"), buildReview("r2", "p1", "u2")));

                StepVerifier.create(service.getReviewsByProduct("p1"))
                                .expectNextCount(2)
                                .verifyComplete();
        }

        @Test
        void getProductRating_withReviews_calculatesCorrectly() {
                Review r1 = buildReview("r1", "p1", "u1");
                r1.setRating(4);
                Review r2 = buildReview("r2", "p1", "u2");
                r2.setRating(5);
                Review r3 = buildReview("r3", "p1", "u3");
                r3.setRating(3);

                when(reviewRepository.findByProductIdOrderByCreatedAtDesc("p1"))
                                .thenReturn(Flux.just(r1, r2, r3));

                StepVerifier.create(service.getProductRating("p1"))
                                .expectNextMatches(rating -> "p1".equals(rating.productId()) &&
                                                rating.averageRating() == 4.0 &&
                                                rating.reviewCount() == 3L)
                                .verifyComplete();
        }

        @Test
        void getProductRating_noReviews_returnsZeros() {
                when(reviewRepository.findByProductIdOrderByCreatedAtDesc("p1"))
                                .thenReturn(Flux.empty());

                StepVerifier.create(service.getProductRating("p1"))
                                .expectNextMatches(rating -> "p1".equals(rating.productId()) &&
                                                rating.averageRating() == 0.0 &&
                                                rating.reviewCount() == 0L)
                                .verifyComplete();
        }

        @Test
        void deleteReview_authorized_deletesSuccessfully() {
                Review review = buildReview("r1", "p1", "u1");
                when(reviewRepository.findById("r1")).thenReturn(Mono.just(review));
                when(reviewRepository.delete(review)).thenReturn(Mono.empty());

                StepVerifier.create(service.deleteReview("r1", "u1"))
                                .verifyComplete();

                verify(reviewRepository).delete(review);
        }

        @Test
        void deleteReview_unauthorized_throwsReviewNotFoundException() {
                Review review = buildReview("r1", "p1", "u1");
                when(reviewRepository.findById("r1")).thenReturn(Mono.just(review));

                StepVerifier.create(service.deleteReview("r1", "differentUser"))
                                .expectError(ReviewNotFoundException.class)
                                .verify();

                verify(reviewRepository, never()).delete(any());
        }

        @Test
        void deleteReview_notFound_throwsReviewNotFoundException() {
                when(reviewRepository.findById("missing")).thenReturn(Mono.empty());

                StepVerifier.create(service.deleteReview("missing", "u1"))
                                .expectError(ReviewNotFoundException.class)
                                .verify();
        }

        private Review buildReview(String id, String productId, String userId) {
                Review r = new Review();
                r.setId(id);
                r.setProductId(productId);
                r.setUserId(userId);
                r.setUserName("User " + userId);
                r.setRating(5);
                r.setTitle("Title");
                r.setComment("Comment");
                r.setVerified(false);
                r.setCreatedAt(Instant.now());
                return r;
        }
}
