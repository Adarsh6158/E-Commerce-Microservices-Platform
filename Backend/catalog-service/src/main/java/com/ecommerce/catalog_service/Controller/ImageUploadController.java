package com.ecommerce.catalog_service.Controller;

import com.ecommerce.catalog_service.Dto.Response.ImageUploadResponse;
import com.ecommerce.catalog_service.Service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@Tag(name = "Image Upload", description = "Product image upload")
public class ImageUploadController {

    private final ImageService imageService;

    public ImageUploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload product images", description = "Uploads one or more images for a product and updates the gallery")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Images uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid file type")
    })
    public Mono<ImageUploadResponse> uploadImages(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @RequestPart("files") Flux<FilePart> files) {

        return imageService.storeImages(productId, files)
                .collectList()
                .map(urls -> new ImageUploadResponse(productId, urls));
    }
}
