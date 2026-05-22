package com.ecommerce.search_service.Service.Impl;

import com.ecommerce.search_service.Config.CatalogProperties;
import com.ecommerce.search_service.Domain.ProductDocument;
import com.ecommerce.search_service.Exception.IndexingException;
import com.ecommerce.search_service.Repository.ProductSearchRepository;
import com.ecommerce.search_service.Service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class IndexingServiceImpl implements IndexingService {

        private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);

        private final ProductSearchRepository searchRepository;
        private final WebClient catalogWebClient;
        private final CatalogProperties catalogProperties;

        public IndexingServiceImpl(ProductSearchRepository searchRepository,
                        @Qualifier("catalogWebClient") WebClient catalogWebClient,
                        CatalogProperties catalogProperties) {
                this.searchRepository = searchRepository;
                this.catalogWebClient = catalogWebClient;
                this.catalogProperties = catalogProperties;
        }

        @Override
        public Mono<ProductDocument> indexProduct(ProductDocument document) {
                return Mono.fromCallable(() -> searchRepository.save(document))
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnSuccess(d -> log.info("Product indexed: id={}", d.getId()))
                                .onErrorMap(e -> new IndexingException("Failed to index product: " + document.getId(),
                                                e));
        }

        @Override
        public Mono<Void> deleteProduct(String productId) {
                return Mono.fromRunnable(() -> searchRepository.deleteById(productId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .then()
                                .doOnSuccess(v -> log.info("Product removed from index: id={}", productId))
                                .onErrorMap(e -> new IndexingException("Failed to delete product: " + productId, e));
        }

        @Override
        public Mono<Long> reindexAll() {
                log.info("Starting full reindex from catalog service…");

                return Mono.fromCallable(() -> {
                        searchRepository.deleteAll();
                        return 0L;
                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .then(fetchAndIndexFromCatalog());
        }

        private Mono<Long> fetchAndIndexFromCatalog() {
                return catalogWebClient.get()
                                .uri(catalogProperties.getProductsPath())
                                .retrieve()
                                .bodyToFlux(ProductDocument.class)
                                .flatMap(this::indexProduct)
                                .count()
                                .doOnSuccess(count -> log.info("Reindex completed. {} products indexed.", count))
                                .doOnError(e -> log.error("Reindex failed", e));
        }
}
