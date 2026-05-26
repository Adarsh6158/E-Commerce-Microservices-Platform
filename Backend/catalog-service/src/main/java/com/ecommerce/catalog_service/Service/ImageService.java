package com.ecommerce.catalog_service.Service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

public interface ImageService {

    Flux<String> storeImages(String productId, Flux<FilePart> files);

    Path resolveImage(String filename);
}
