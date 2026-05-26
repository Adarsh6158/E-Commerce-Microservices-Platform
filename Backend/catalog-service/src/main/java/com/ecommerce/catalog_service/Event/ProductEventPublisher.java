package com.ecommerce.catalog_service.Event;

import com.ecommerce.catalog_service.Constant.EventConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(ProductEvent event) {
        String topic = switch (event.eventType()) {
            case EventConstants.EVENT_TYPE_CREATED -> EventConstants.TOPIC_PRODUCT_CREATED;
            case EventConstants.EVENT_TYPE_UPDATED -> EventConstants.TOPIC_PRODUCT_UPDATED;
            case EventConstants.EVENT_TYPE_DELETED -> EventConstants.TOPIC_PRODUCT_DELETED;
            default -> EventConstants.TOPIC_PRODUCT_UPDATED;
        };

        try {
            String payload = objectMapper.writeValueAsString(event);
            String key = event.productId().toString();

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, key, payload);

            record.headers().add(new RecordHeader(
                    EventConstants.HEADER_CORRELATION_ID,
                    event.correlationId().getBytes(StandardCharsets.UTF_8)
            ));

            record.headers().add(new RecordHeader(
                    EventConstants.HEADER_EVENT_TYPE,
                    event.eventType().getBytes(StandardCharsets.UTF_8)
            ));

            kafkaTemplate.send(record)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error(
                                    "Failed to publish product event. productId={}, eventType={}, correlationId={}",
                                    event.productId(),
                                    event.eventType(),
                                    event.correlationId(),
                                    ex
                            );
                        } else {
                            log.info(
                                    "Product event published. topic={}, partition={}, offset={}, productId={}, correlationId={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    event.productId(),
                                    event.correlationId()
                            );
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error(
                    "Failed to serialize product event. productId={}",
                    event.productId(),
                    e
            );
        }
    }
}
