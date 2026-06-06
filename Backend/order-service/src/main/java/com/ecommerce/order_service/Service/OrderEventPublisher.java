package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;

public interface OrderEventPublisher {

    void publishOrderEvent(String topic, Order order);
}
