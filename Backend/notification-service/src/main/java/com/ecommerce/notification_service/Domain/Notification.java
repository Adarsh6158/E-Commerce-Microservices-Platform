package com.ecommerce.notification_service.Domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("notifications")
public class Notification {

    @Id
    private UUID id;
    private String userId;
    private String orderId;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String body;
    private NotificationStatus status;
    private String failureReason;
    private int retryCount;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public enum NotificationType {
        ORDER_CONFIRMED, ORDER_CANCELLED, ORDER_FAILED,
        PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_REFUNDED,
        SHIPPING_UPDATE, GENERAL
    }

    public enum NotificationChannel {
        EMAIL, SMS, PUSH, IN_APP
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, RETRYING
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
