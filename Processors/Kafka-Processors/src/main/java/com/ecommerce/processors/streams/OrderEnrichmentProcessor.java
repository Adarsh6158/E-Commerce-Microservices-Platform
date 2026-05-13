package com.ecommerce.processors.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class OrderEnrichmentProcessor {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-enrichment-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> ordersStream = builder.stream("order.created");

        KStream<String, String> enrichedOrders = ordersStream.mapValues(value -> {
            if (value == null || value.isEmpty()) return value;
            if (value.endsWith("}")) {
                return value.substring(0, value.length() - 1) + ", \"enriched\": true, \"enrichmentSource\": \"OrderEnrichmentProcessor\"}";
            }
            return value;
        });

        enrichedOrders.to("order.enriched");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            System.out.println("Started OrderEnrichmentProcessor Stream...");
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
