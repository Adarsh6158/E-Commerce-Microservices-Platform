package com.ecommerce.notification_service.Service;

import com.ecommerce.notification_service.Domain.Notification;
import com.ecommerce.notification_service.Repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Mono<Notification> sendNotification(String userId, String orderId,
                                               Notification.NotificationType type,
                                               String subject, String body) {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification)
                .flatMap(this::simulateDelivery)
                .doOnSuccess(n -> log.info("Notification sent: id={}, type={}, userId={}, orderId={}",
                        n.getId(), n.getType(), n.getUserId(), n.getOrderId()));
    }

    private Mono<Notification> simulateDelivery(Notification notification) {
        // Simulate async delivery — in production this would call email/SMS/push APIs
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        log.debug("Simulated delivery for notification: type={}, channel={}, userId={}",
                notification.getType(), notification.getChannel(), notification.getUserId());

        return notificationRepository.save(notification);
    }

    public Flux<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Flux<Notification> getOrderNotifications(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    public Mono<Notification> getNotification(UUID id) {
        return notificationRepository.findById(id);
    }
}
