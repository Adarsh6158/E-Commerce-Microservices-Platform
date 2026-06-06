package com.ecommerce.order_service.Service.Impl;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Repository.OrderRepository;
import com.ecommerce.order_service.Service.OrderEventPublisher;
import com.ecommerce.order_service.Service.OrderSagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.ecommerce.order_service.Constant.AppConstants.INVENTORY_RESERVATION_FAILED_PREFIX;
import static com.ecommerce.order_service.Constant.AppConstants.PAYMENT_FAILED_PREFIX;
import static com.ecommerce.order_service.Constant.EventConstants.*;
import static com.ecommerce.order_service.Constant.OrderStatusConstants.*;

@Service
public class OrderSagaServiceImpl implements OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderSagaServiceImpl(OrderRepository orderRepository,
                                OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, String newStatus, String failureReason) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(newStatus);
                    order.setUpdatedAt(Instant.now());

                    if (failureReason != null) {
                        order.setFailureReason(failureReason);
                    }

                    return orderRepository.save(order);
                })
                .doOnSuccess(order ->
                        log.info("Order status updated: orderId={}, status={}", orderId, newStatus));
    }

    @Override
    public Mono<Order> handleInventoryReserved(String orderId) {
        return updateOrderStatus(orderId, INVENTORY_RESERVED, null)
                .doOnSuccess(order -> {
                    log.info("Inventory reserved for order: {}", orderId);
                    updateOrderStatus(orderId, PAYMENT_PROCESSING, null)
                            .doOnSuccess(o -> orderEventPublisher.publishOrderEvent(TOPIC_ORDER_PAYMENT_REQUESTED, o))
                            .subscribe();
                });
    }

    @Override
    public Mono<Order> handleInventoryReserveFailed(String orderId, String reason) {
        return updateOrderStatus(orderId, FAILED, INVENTORY_RESERVATION_FAILED_PREFIX + reason)
                .doOnSuccess(order -> {
                    log.warn("Inventory reservation failed for order: {}, reason: {}", orderId, reason);
                    orderEventPublisher.publishOrderEvent(TOPIC_ORDER_FAILED, order);
                });
    }

    @Override
    public Mono<Order> handlePaymentCompleted(String orderId) {
        return updateOrderStatus(orderId, CONFIRMED, null)
                .doOnSuccess(order -> {
                    log.info("Payment completed, order confirmed: {}", orderId);
                    orderEventPublisher.publishOrderEvent(TOPIC_ORDER_CONFIRMED, order);
                });
    }

    @Override
    public Mono<Order> handlePaymentFailed(String orderId, String reason) {
        return updateOrderStatus(orderId, FAILED, PAYMENT_FAILED_PREFIX + reason)
                .doOnSuccess(order -> {
                    log.warn("Payment failed for order: {}, triggering compensation", orderId);
                    orderEventPublisher.publishOrderEvent(TOPIC_ORDER_CANCELLED, order);
                });
    }
}
