package com.ecommerce.notification_service.Controller;

import com.ecommerce.notification_service.Dto.DtoMapper;
import com.ecommerce.notification_service.Dto.NotificationDto;
import com.ecommerce.notification_service.Service.NotificationService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Flux<NotificationDto> getUserNotifications(@RequestHeader("X-User-Id") String userId) {
        return notificationService.getUserNotifications(userId).map(DtoMapper::toDto);
    }

    @GetMapping("/{id}")
    public Mono<NotificationDto> getNotification(@PathVariable UUID id) {
        return notificationService.getNotification(id).map(DtoMapper::toDto);
    }

    @GetMapping("/order/{orderId}")
    public Flux<NotificationDto> getOrderNotifications(@PathVariable String orderId) {
        return notificationService.getOrderNotifications(orderId).map(DtoMapper::toDto);
    }
}