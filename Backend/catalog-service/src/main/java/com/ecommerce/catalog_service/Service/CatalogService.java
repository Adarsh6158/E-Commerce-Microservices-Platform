package com.ecommerce.catalog_service.Service;

import com.ecommerce.catalog_service.Domain.Product;
import com.ecommerce.catalog_service.Domain.Category;
import com.ecommerce.catalog_service.Event.ProductEvent;
import com.ecommerce.catalog_service.Event.ProductEventPublisher;
import com.ecommerce.catalog_service.Repository.ProductRepository;
import com.ecommerce.catalog_service.Repository.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
    private static final String CACHE_PREFIX = "product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ProductEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public CatalogService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ReactiveStringRedisTemplate redisTemplate,
                          ProductEventPublisher eventPublisher,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    // Product APIs

    public Mono<Product> getById(String id) {
        String cacheKey = CACHE_PREFIX + id;

        return redisTemplate.opsForValue().get(cacheKey)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, Product.class));
                    } catch (JsonProcessingException e) {
                        log.warn("Cache deserialization failed for key={}", cacheKey);
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(product ->
                                        cacheProduct(cacheKey, product).thenReturn(product)
                                )
                )
                .doOnNext(p -> log.debug("Product fetched id={}", p.getId()));
    }

    public Mono<Product> getBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Flux<Product> getByCategory(String categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    public Flux<Product> getByBrand(String brand) {
        return productRepository.findByBrandAndActiveTrue(brand);
    }

    public Flux<Product> getAllActive() {
        return productRepository.findByActiveTrue();
    }

    public Flux<String> getDistinctBrands() {
        return productRepository.findByActiveTrue()
                .map(Product::getBrand)
                .filter(brand -> brand != null && !brand.trim().isEmpty())
                .distinct();
    }

    public Mono<Product> create(Product product, String correlationId) {
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        return productRepository.save(product)
                .doOnSuccess(saved -> {
                    eventPublisher.publish(ProductEvent.created(
                            saved.getId(),
                            saved.getSku(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getBrand(),
                            saved.getCategoryId(),
                            saved.getBasePrice(),
                            saved.getImage(),
                            saved.getThumbnail(),
                            saved.getGalleryImages(),
                            saved.getAltText(),
                            saved.getAttributes(),
                            correlationId
                    ));
                    log.info("Product created id={}", saved.getId());
                });
    }

    public Mono<Product> update(String id, Product updates, String correlationId) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found: " + id)))
                .flatMap(existing -> {
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getBrand() != null) existing.setBrand(updates.getBrand());
                    if (updates.getBasePrice() != null) existing.setBasePrice(updates.getBasePrice());
                    if (updates.getImage() != null) existing.setImage(updates.getImage());
                    if (updates.getThumbnail() != null) existing.setThumbnail(updates.getThumbnail());
                    if (updates.getGalleryImages() != null) existing.setGalleryImages(updates.getGalleryImages());
                    if (updates.getAltText() != null) existing.setAltText(updates.getAltText());
                    if (updates.getAttributes() != null) existing.setAttributes(updates.getAttributes());
                    if (updates.getCategoryId() != null) existing.setCategoryId(updates.getCategoryId());

                    existing.setActive(updates.isActive());
                    existing.setUpdatedAt(Instant.now());

                    return productRepository.save(existing);
                })
                .flatMap(saved ->
                        redisTemplate.delete(CACHE_PREFIX + id)
                                .doOnSuccess(v -> {
                                    eventPublisher.publish(ProductEvent.updated(
                                            saved.getId(),
                                            saved.getSku(),
                                            saved.getName(),
                                            saved.getDescription(),
                                            saved.getBrand(),
                                            saved.getCategoryId(),
                                            saved.getBasePrice(),
                                            saved.getImage(),
                                            saved.getThumbnail(),
                                            saved.getGalleryImages(),
                                            saved.getAltText(),
                                            saved.isActive(),
                                            saved.getAttributes(),
                                            correlationId
                                    ));
                                    log.info("Product updated id={}", id);
                                })
                                .thenReturn(saved)
                );
    }

    public Mono<Void> delete(String id, String correlationId) {
        return productRepository.deleteById(id)
                .then(redisTemplate.delete(CACHE_PREFIX + id))
                .doOnSuccess(v -> {
                    eventPublisher.publish(ProductEvent.deleted(id, correlationId));
                    log.info("Product deleted id={}", id);
                })
                .then();
    }

    // Category APIs 

    public Flux<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public Mono<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public Mono<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    public Flux<Category> getCategoryChildren(String parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Mono<Category> createCategory(Category category) {
        category.setActive(true);
        category.setCreatedAt(Instant.now());
        return categoryRepository.save(category);
    }

    // Internal

    private Mono<Boolean> cacheProduct(String key, Product product) {
        try {
            String json = objectMapper.writeValueAsString(product);
            return redisTemplate.opsForValue()
                    .set(key, json, CACHE_TTL)
                    .onErrorReturn(false);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize product for cache key={}", key);
            return Mono.just(false);
        }
    }
}
