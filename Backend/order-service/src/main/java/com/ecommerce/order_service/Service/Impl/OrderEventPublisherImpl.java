package com.ecommerce.order_service.Service.Impl;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Service.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisherImpl implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisherImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderMapper orderMapper;

    public OrderEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate,
                                   OrderMapper orderMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderMapper = orderMapper;
    }

    @Override
    public void publishOrderEvent(String topic, Order order) {
        try {
            var event = orderMapper.toEventPayload(order);
            kafkaTemplate.send(topic, order.getId(), event);
            log.debug("Published event to {}: orderId={}", topic, order.getId());
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", topic, e.getMessage(), e);
        }
    }
}
