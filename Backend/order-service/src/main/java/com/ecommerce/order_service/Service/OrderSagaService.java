package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;
import reactor.core.publisher.Mono;

public interface OrderSagaService {

    Mono<Order> updateOrderStatus(String orderId, String newStatus, String failureReason);

    Mono<Order> handleInventoryReserved(String orderId);

    Mono<Order> handleInventoryReserveFailed(String orderId, String reason);

    Mono<Order> handlePaymentCompleted(String orderId);

    Mono<Order> handlePaymentFailed(String orderId, String reason);
}
