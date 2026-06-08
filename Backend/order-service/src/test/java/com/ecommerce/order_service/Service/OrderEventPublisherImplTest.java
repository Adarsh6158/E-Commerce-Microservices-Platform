package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Dto.Event.OrderEventPayload;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Service.Impl.OrderEventPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderMapper orderMapper;

    private OrderEventPublisherImpl publisher;

    @BeforeEach
    void setUp() {
        publisher = new OrderEventPublisherImpl(kafkaTemplate, orderMapper);
    }

    @Test
    void publishOrderEvent_shouldSendToKafka() {
        Order order = buildOrder("order-1");
        OrderEventPayload payload = new OrderEventPayload(
                "order-1", "user-1", "CONFIRMED",
                new BigDecimal("100.00"), "corr-1", null, List.of());

        when(orderMapper.toEventPayload(order)).thenReturn(payload);

        publisher.publishOrderEvent("order.confirmed", order);

        verify(kafkaTemplate).send("order.confirmed", "order-1", payload);
    }

    @Test
    void publishOrderEvent_shouldHandleExceptionGracefully() {
        Order order = buildOrder("order-1");

        when(orderMapper.toEventPayload(order)).thenThrow(new RuntimeException("mapping failed"));

        publisher.publishOrderEvent("order.created", order);

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void publishOrderEvent_shouldHandleKafkaFailure() {
        Order order = buildOrder("order-1");
        OrderEventPayload payload = new OrderEventPayload(
                "order-1", "user-1", "PENDING",
                new BigDecimal("100.00"), "corr-1", null, List.of());

        when(orderMapper.toEventPayload(order)).thenReturn(payload);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenThrow(new RuntimeException("kafka down"));

        publisher.publishOrderEvent("order.created", order);

        verify(kafkaTemplate).send("order.created", "order-1", payload);
    }

    private Order buildOrder(String id) {
        Order order = new Order();
        order.setId(id);
        order.setUserId("user-1");
        order.setStatus("CONFIRMED");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of());
        return order;
    }
}
