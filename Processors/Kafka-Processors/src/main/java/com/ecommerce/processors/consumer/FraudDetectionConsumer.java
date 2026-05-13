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

public class FraudDetectionConsumer {
    private static final Logger log = LoggerFactory.getLogger(FraudDetectionConsumer.class);
    private static final double FRAUD_THRESHOLD = ConfigLoader.getDouble("fraud.detection.threshold", 5000.00);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", ConfigLoader.get("kafka.bootstrap.servers", "localhost:9092"));
        props.put("group.id", "fraud-detection-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("order.created"));
        ObjectMapper mapper = new ObjectMapper();

        log.info("Started Fraud Detection Consumer...");
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        JsonNode node = mapper.readTree(record.value());
                        double amount = node.has("totalAmount") ? node.get("totalAmount").asDouble() : 0.0;
                        if (amount > FRAUD_THRESHOLD) {
                            log.warn("FRAUD ALERT! Order {} has unusually high amount: {}", record.key(), amount);
                        } else {
                            log.info("Order {} processed. Amount: {} (Safe)", record.key(), amount);
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
