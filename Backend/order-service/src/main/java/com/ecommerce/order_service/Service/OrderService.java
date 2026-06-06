package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {

    Mono<Order> createOrder(String userId, List<CreateOrderRequest.Item> items);

    Mono<Order> getOrder(String orderId);

    Flux<Order> getOrdersByUserId(String userId);

    Mono<Order> cancelOrder(String orderId, String userId);

    Mono<Boolean> hasUserPurchasedProduct(String userId, String productId);
}
