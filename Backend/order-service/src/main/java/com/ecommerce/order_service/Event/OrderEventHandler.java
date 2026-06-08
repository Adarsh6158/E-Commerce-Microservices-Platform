package com.ecommerce.order_service.Event;

import com.ecommerce.order_service.Dto.Event.InventoryEventPayload;
import com.ecommerce.order_service.Dto.Event.PaymentEventPayload;
import com.ecommerce.order_service.Service.OrderSagaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.ecommerce.order_service.Constant.AppConstants.REASON_UNKNOWN;
import static com.ecommerce.order_service.Constant.EventConstants.*;

@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);
    private final OrderSagaService orderSagaService;
    private final ObjectMapper objectMapper;

    public OrderEventHandler(OrderSagaService orderSagaService, ObjectMapper objectMapper) {
        this.orderSagaService = orderSagaService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = TOPIC_INVENTORY_RESERVED, groupId = GROUP_ORDER_SAGA)
    public void handleInventoryReserved(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                        Acknowledgment ack) {
        try {
            InventoryEventPayload payload = objectMapper.readValue(message, InventoryEventPayload.class);
            log.info("Received inventory.reserved for orderId={}", payload.orderId());

            orderSagaService.handleInventoryReserved(payload.orderId())
                    .doOnSuccess(order -> ack.acknowledge())
                    .doOnError(e -> log.error("Error handling inventory.reserved: {}", e.getMessage(), e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to process inventory.reserved event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = TOPIC_INVENTORY_RESERVE_FAILED, groupId = GROUP_ORDER_SAGA)
    public void handleInventoryReserveFailed(@Payload String message,
                                             @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                             Acknowledgment ack) {
        try {
            InventoryEventPayload payload = objectMapper.readValue(message, InventoryEventPayload.class);
            String reason = payload.reason() != null ? payload.reason() : REASON_UNKNOWN;
            log.info("Received inventory.reserve-failed for orderId={}, reason={}", payload.orderId(), reason);

            orderSagaService.handleInventoryReserveFailed(payload.orderId(), reason)
                    .doOnSuccess(order -> ack.acknowledge())
                    .doOnError(e -> log.error("Error handling inventory.reserve-failed: {}", e.getMessage(), e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to process inventory.reserve-failed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = TOPIC_PAYMENT_COMPLETED, groupId = GROUP_ORDER_SAGA)
    public void handlePaymentCompleted(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                       Acknowledgment ack) {
        try {
            PaymentEventPayload payload = objectMapper.readValue(message, PaymentEventPayload.class);
            log.info("Received payment.completed for orderId={}", payload.orderId());

            orderSagaService.handlePaymentCompleted(payload.orderId())
                    .doOnSuccess(order -> ack.acknowledge())
                    .doOnError(e -> log.error("Error handling payment.completed: {}", e.getMessage(), e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to process payment.completed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = TOPIC_PAYMENT_FAILED, groupId = GROUP_ORDER_SAGA)
    public void handlePaymentFailed(@Payload String message,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                    Acknowledgment ack) {
        try {
            PaymentEventPayload payload = objectMapper.readValue(message, PaymentEventPayload.class);
            String reason = payload.reason() != null ? payload.reason() : REASON_UNKNOWN;
            log.info("Received payment.failed for orderId={}, reason={}", payload.orderId(), reason);

            orderSagaService.handlePaymentFailed(payload.orderId(), reason)
                    .doOnSuccess(order -> ack.acknowledge())
                    .doOnError(e -> log.error("Error handling payment.failed: {}", e.getMessage(), e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to process payment.failed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}