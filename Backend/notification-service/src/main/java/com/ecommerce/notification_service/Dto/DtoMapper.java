package com.ecommerce.notification_service.Dto;

import com.ecommerce.notification_service.Domain.Notification;

public final class DtoMapper {

    private DtoMapper() {}

    public static NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getUserId(),
                n.getOrderId(),
                n.getType(),
                n.getChannel(),
                n.getSubject(),
                n.getBody(),
                n.getStatus(),
                n.getFailureReason(),
                n.getRetryCount(),
                n.getSentAt(),
                n.getCreatedAt()
        );
    }
}
