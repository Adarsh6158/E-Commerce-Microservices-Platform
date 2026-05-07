package com.ecommerce.notification_service.Event;

import com.ecommerce.notification_service.Domain.Notification;
import com.ecommerce.notification_service.Service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventHandler(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.confirmed", groupId = "notification-service")
    public void handleOrderConfirmed(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                     Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String orderId = payload.get("orderId").asText();
            String userId = payload.get("userId").asText();

            notificationService.sendNotification(
                            userId, orderId,
                            Notification.NotificationType.ORDER_CONFIRMED,
                            "Order Confirmed - #" + orderId.substring(0, 8),
                            "Your order #" + orderId.substring(0, 8) + " has been confirmed and is being processed."
                    ).doOnSuccess(n -> ack.acknowledge())
                    .doOnError(e -> log.error("Failed to send order confirmed notification: {}", e.getMessage(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process order.confirmed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "notification-service")
    public void handleOrderCancelled(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                     Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String orderId = payload.get("orderId").asText();
            String userId = payload.get("userId").asText();
            String reason = payload.has("failureReason") && !payload.get("failureReason").isNull()
                    ? payload.get("failureReason").asText()
                    : "No reason provided";

            notificationService.sendNotification(
                            userId, orderId,
                            Notification.NotificationType.ORDER_CANCELLED,
                            "Order Cancelled - #" + orderId.substring(0, 8),
                            "Your order #" + orderId.substring(0, 8) + " has been cancelled. Reason: " + reason
                    ).doOnSuccess(n -> ack.acknowledge())
                    .doOnError(e -> log.error("Failed to send order cancelled notification: {}", e.getMessage(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process order.cancelled event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "order.failed", groupId = "notification-service")
    public void handleOrderFailed(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String orderId = payload.get("orderId").asText();
            String userId = payload.get("userId").asText();
            String reason = payload.has("failureReason") && !payload.get("failureReason").isNull()
                    ? payload.get("failureReason").asText()
                    : "An unexpected error occurred";

            notificationService.sendNotification(
                            userId, orderId,
                            Notification.NotificationType.ORDER_FAILED,
                            "Order Failed - #" + orderId.substring(0, 8),
                            "Your order #" + orderId.substring(0, 8) + " could not be processed. Reason: " + reason
                    ).doOnSuccess(n -> ack.acknowledge())
                    .doOnError(e -> log.error("Failed to send order failed notification: {}", e.getMessage(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process order.failed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-service")
    public void handlePaymentCompleted(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                       Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String orderId = payload.get("orderId").asText();
            String amount = payload.has("amount") ? payload.get("amount").asText() : "N/A";

            notificationService.sendNotification(
                            "system", orderId,
                            Notification.NotificationType.PAYMENT_COMPLETED,
                            "Payment Received - Order #" + orderId.substring(0, 8),
                            "Payment of $" + amount + " received for order #" + orderId.substring(0, 8) + "."
                    ).doOnSuccess(n -> ack.acknowledge())
                    .doOnError(e -> log.error("Failed to send payment completed notification: {}", e.getMessage(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process payment.completed event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "payment.refunded", groupId = "notification-service")
    public void handlePaymentRefunded(@Payload String message,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                      Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            String orderId = payload.get("orderId").asText();
            String amount = payload.has("amount") ? payload.get("amount").asText() : "N/A";

            notificationService.sendNotification(
                            "system", orderId,
                            Notification.NotificationType.PAYMENT_REFUNDED,
                            "Refund Processed - Order #" + orderId.substring(0, 8),
                            "A refund of $" + amount + " has been processed for order #" + orderId.substring(0, 8) + "."
                    ).doOnSuccess(n -> ack.acknowledge())
                    .doOnError(e -> log.error("Failed to send payment refunded notification: {}", e.getMessage(), e))
                    .subscribe();

        } catch (Exception e) {
            log.error("Failed to process payment.refunded event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }
}
