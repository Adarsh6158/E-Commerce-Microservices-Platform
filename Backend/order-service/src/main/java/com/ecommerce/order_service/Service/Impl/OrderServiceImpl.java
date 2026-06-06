package com.ecommerce.order_service.Service.Impl;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Repository.OrderRepository;
import com.ecommerce.order_service.Service.OrderEventPublisher;
import com.ecommerce.order_service.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.ecommerce.order_service.Constant.AppConstants.CANCELLED_BY_USER;
import static com.ecommerce.order_service.Constant.EventConstants.TOPIC_ORDER_CANCELLED;
import static com.ecommerce.order_service.Constant.EventConstants.TOPIC_ORDER_CREATED;
import static com.ecommerce.order_service.Constant.OrderStatusConstants.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderEventPublisher orderEventPublisher,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.orderMapper = orderMapper;
    }

    @Override
    public Mono<Order> createOrder(String userId, List<CreateOrderRequest.Item> items) {
        String correlationId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(PENDING);
        order.setCorrelationId(correlationId);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        BigDecimal totalAmount = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);

        List<OrderItem> orderItems = orderMapper.toOrderItems(items);
        order.setItems(orderItems);

        return orderRepository.save(order)
                .doOnSuccess(savedOrder -> {
                    log.info("Order created: orderId={}, correlationId={}", savedOrder.getId(), correlationId);
                    orderEventPublisher.publishOrderEvent(TOPIC_ORDER_CREATED, savedOrder);
                });
    }

    @Override
    public Mono<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Mono<Order> cancelOrder(String orderId, String userId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .filter(order -> PENDING.equals(order.getStatus())
                        || INVENTORY_RESERVED.equals(order.getStatus()))
                .flatMap(order -> {
                    order.setStatus(CANCELLED);
                    order.setUpdatedAt(Instant.now());
                    order.setFailureReason(CANCELLED_BY_USER);
                    return orderRepository.save(order);
                })
                .doOnSuccess(order -> {
                    if (order != null) {
                        log.info("Order cancelled by user: {}", orderId);
                        orderEventPublisher.publishOrderEvent(TOPIC_ORDER_CANCELLED, order);
                    }
                });
    }

    @Override
    public Mono<Boolean> hasUserPurchasedProduct(String userId, String productId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .filter(order -> CONFIRMED.equals(order.getStatus()))
                .any(order -> order.getItems() != null &&
                        order.getItems().stream()
                                .anyMatch(item -> productId.equals(item.getProductId())));
    }
}
