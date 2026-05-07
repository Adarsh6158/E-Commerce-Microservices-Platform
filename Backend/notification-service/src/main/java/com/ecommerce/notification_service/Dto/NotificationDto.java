package com.ecommerce.notification_service.Dto;

import com.ecommerce.notification_service.Domain.Notification.NotificationChannel;
import com.ecommerce.notification_service.Domain.Notification.NotificationStatus;
import com.ecommerce.notification_service.Domain.Notification.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String userId,
        String orderId,
        NotificationType type,
        NotificationChannel channel,
        String subject,
        String body,
        NotificationStatus status,
        String failureReason,
        int retryCount,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {}
