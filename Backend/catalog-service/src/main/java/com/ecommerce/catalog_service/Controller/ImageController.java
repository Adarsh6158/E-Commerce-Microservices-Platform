package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Service.ImageService;
import com.ecommerce.catalog_service.Validator.ImageValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@RestController
@RequestMapping("/products/images")
@Tag(name = "Images", description = "Product image retrieval")
public class ImageController {

    private final ImageService imageService;
    private final ImageValidator imageValidator;

    public ImageController(ImageService imageService, ImageValidator imageValidator) {
        this.imageService = imageService;
        this.imageValidator = imageValidator;
    }

    @GetMapping("/{filename}")
    @Operation(summary = "Get product image", description = "Retrieves a product image by filename with 7-day cache headers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image found"),
            @ApiResponse(responseCode = "400", description = "Invalid filename"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public Mono<ResponseEntity<Resource>> getImage(
            @Parameter(description = "Image filename") @PathVariable String filename) {

        imageValidator.validateFilename(filename);

        return Mono.fromCallable(() -> {
            Path path = imageService.resolveImage(filename);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().<Resource>build();
            }
            String contentType = Files.probeContentType(path);
            MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
            Resource resource = new FileSystemResource(path);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(resource);
        });
    }
}
