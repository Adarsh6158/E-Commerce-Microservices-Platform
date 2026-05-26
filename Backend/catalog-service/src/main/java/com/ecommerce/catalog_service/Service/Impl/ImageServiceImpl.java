package com.ecommerce.catalog_service.Service.Impl;

import com.ecommerce.catalog_service.Config.CatalogProperties;
import com.ecommerce.catalog_service.Exception.ImageStorageException;
import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import com.ecommerce.catalog_service.Repository.ProductRepository;
import com.ecommerce.catalog_service.Service.ImageService;
import com.ecommerce.catalog_service.Validator.ImageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final Path uploadDir;
    private final String baseUrl;
    private final CatalogProperties properties;
    private final ProductRepository productRepository;
    private final ImageValidator imageValidator;

    public ImageServiceImpl(CatalogProperties properties,
                            ProductRepository productRepository,
                            ImageValidator imageValidator) {
        this.properties = properties;
        this.productRepository = productRepository;
        this.imageValidator = imageValidator;
        this.uploadDir = Paths.get(properties.getImages().getUploadDir()).toAbsolutePath().normalize();
        this.baseUrl = properties.getImages().getBaseUrl();

        try {
            Files.createDirectories(this.uploadDir);
            log.info("Image upload directory: {}", this.uploadDir);
        } catch (IOException e) {
            throw new ImageStorageException("Cannot create upload directory: " + this.uploadDir, e);
        }
    }

    @Override
    public Flux<String> storeImages(String productId, Flux<FilePart> files) {
        return files
                .flatMap(file -> storeOne(productId, file))
                .collectList()
                .flatMapMany(urls ->
                        updateProductImages(productId, urls)
                                .thenMany(Flux.fromIterable(urls))
                );
    }

    @Override
    public Path resolveImage(String filename) {
        Path resolved = uploadDir.resolve(filename).normalize();

        if (!resolved.startsWith(uploadDir)) {
            throw new InvalidRequestException("Invalid path");
        }

        return resolved;
    }



    private Mono<String> storeOne(String productId, FilePart filePart) {

        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : "";

        imageValidator.validateContentType(contentType, properties.getImages().getAllowedTypes());


        String originalName = Optional.ofNullable(filePart.filename()).orElse("file");
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";


        String filename = productId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path target = uploadDir.resolve(filename).normalize();


        if (!target.startsWith(uploadDir)) {
            return Mono.error(new InvalidRequestException("Invalid file path"));
        }


        return filePart.transferTo(target)
                .then(Mono.fromCallable(() -> {
                    String url = baseUrl + "/" + filename;
                    log.info("Image stored: {} -> {}", originalName, url);
                    return url;
                }));
    }

    private Mono<Void> updateProductImages(String productId, List<String> newUrls) {
        return productRepository.findById(productId)
                .flatMap(product -> {
                    List<String> existing = product.getGalleryImages() != null
                            ? new ArrayList<>(product.getGalleryImages())
                            : new ArrayList<>();

                    existing.addAll(newUrls);
                    product.setGalleryImages(existing);


                    if ((product.getImage() == null || product.getImage().isEmpty()) && !newUrls.isEmpty()) {
                        product.setImage(newUrls.get(0));
                    }

                    return productRepository.save(product);
                })
                .then();
    }
}
