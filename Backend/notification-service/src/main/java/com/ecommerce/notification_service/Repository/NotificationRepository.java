package com.ecommerce.notification_service.Repository;

import com.ecommerce.notification_service.Domain.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {

    Flux<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    Flux<Notification> findByOrderId(String orderId);

    Flux<Notification> findByStatus(Notification.NotificationStatus status);
}
