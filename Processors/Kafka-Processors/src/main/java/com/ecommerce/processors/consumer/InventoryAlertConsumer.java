package com.ecommerce.processors.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ecommerce.processors.ConfigLoader;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class InventoryAlertConsumer {
    private static final Logger log = LoggerFactory.getLogger(InventoryAlertConsumer.class);
    private static final int LOW_STOCK_THRESHOLD = ConfigLoader.getInt("inventory.low.stock.threshold", 10);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", ConfigLoader.get("kafka.bootstrap.servers", "localhost:9092"));
        props.put("group.id", "inventory-alert-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("inventory.updated"));
        ObjectMapper mapper = new ObjectMapper();

        log.info("Started Inventory Alert Consumer...");
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        JsonNode node = mapper.readTree(record.value());
                        int quantity = node.has("quantity") ? node.get("quantity").asInt() : 0;
                        String productId = node.has("productId") ? node.get("productId").asText() : record.key();
                        
                        if (quantity < LOW_STOCK_THRESHOLD) {
                            log.warn("LOW STOCK ALERT! Product {} has only {} items left.", productId, quantity);
                        } else {
                            log.info("Product {} stock is healthy: {}", productId, quantity);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse record", e);
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }
}
