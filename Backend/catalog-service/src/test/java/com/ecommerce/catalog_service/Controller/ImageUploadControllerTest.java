package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    @Mock private ImageService imageService;

    private ImageUploadController controller;

    @BeforeEach
    void setUp() {
        controller = new ImageUploadController(imageService);
    }

    @Test
    void uploadImages_returnsImageUploadResponse() {
        when(imageService.storeImages(eq("p1"), any()))
                .thenReturn(Flux.just("http://localhost/img1.jpg", "http://localhost/img2.jpg"));

        StepVerifier.create(controller.uploadImages("p1", Flux.empty()))
                .expectNextMatches(response ->
                        "p1".equals(response.productId()) &&
                        response.galleryImages().size() == 2)
                .verifyComplete();
    }
}
