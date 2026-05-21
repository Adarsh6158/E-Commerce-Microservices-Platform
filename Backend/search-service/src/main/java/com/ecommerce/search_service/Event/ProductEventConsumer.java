package com.ecommerce.search_service.Event;

import com.ecommerce.search_service.Dto.Event.ProductEventPayload;
import com.ecommerce.search_service.Mapper.ProductSearchMapper;
import com.ecommerce.search_service.Service.IndexingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.ecommerce.search_service.Constant.EventConstants.*;

@Component
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final IndexingService indexingService;
    private final ProductSearchMapper mapper;
    private final ObjectMapper objectMapper;

    public ProductEventConsumer(IndexingService indexingService,
                                ProductSearchMapper mapper,
                                ObjectMapper objectMapper) {
        this.indexingService = indexingService;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {TOPIC_PRODUCT_CREATED, TOPIC_PRODUCT_UPDATED},
            groupId = GROUP_INDEXER
    )
    public void onProductUpsert(@Payload String payload,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            ProductEventPayload eventPayload = objectMapper.readValue(payload, ProductEventPayload.class);

            indexingService.indexProduct(mapper.toDocument(eventPayload))
                    .doOnSuccess(d -> log.info(
                            "Indexed product from event. type={}, productId={}",
                            eventPayload.getEventType(), eventPayload.getProductId()))
                    .doOnError(e -> log.error(
                            "Failed to index product. type={}, productId={}",
                            eventPayload.getEventType(), eventPayload.getProductId(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process product event. topic={}", topic, e);
        }
    }

    @KafkaListener(
            topics = TOPIC_PRODUCT_DELETED,
            groupId = GROUP_INDEXER
    )
    public void onProductDeleted(@Payload String payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            ProductEventPayload eventPayload = objectMapper.readValue(payload, ProductEventPayload.class);

            indexingService.deleteProduct(eventPayload.getProductId())
                    .doOnSuccess(v -> log.info(
                            "Removed product from index. productId={}", eventPayload.getProductId()))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process product delete event. topic={}", topic, e);
        }
    }
}
