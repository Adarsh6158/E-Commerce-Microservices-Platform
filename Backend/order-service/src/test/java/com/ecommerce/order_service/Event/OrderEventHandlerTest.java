package com.ecommerce.order_service.Event;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Service.OrderSagaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private OrderSagaService orderSagaService;

    @Mock
    private Acknowledgment ack;

    private OrderEventHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new OrderEventHandler(orderSagaService, objectMapper);
    }

    @Test
    void handleInventoryReserved_shouldDelegateToSagaService() {
        String message = "{\"orderId\":\"order-1\"}";
        Order order = buildOrder("order-1");

        when(orderSagaService.handleInventoryReserved("order-1")).thenReturn(Mono.just(order));

        handler.handleInventoryReserved(message, "order-1", ack);

        verify(orderSagaService).handleInventoryReserved("order-1");
    }

    @Test
    void handleInventoryReserved_shouldAcknowledgeOnBadJson() {
        handler.handleInventoryReserved("invalid json", "key", ack);
        verify(ack).acknowledge();
    }

    @Test
    void handleInventoryReserveFailed_shouldDelegateWithReason() {
        String message = "{\"orderId\":\"order-1\",\"reason\":\"Out of stock\"}";
        Order order = buildOrder("order-1");

        when(orderSagaService.handleInventoryReserveFailed("order-1", "Out of stock"))
                .thenReturn(Mono.just(order));

        handler.handleInventoryReserveFailed(message, "order-1", ack);

        verify(orderSagaService).handleInventoryReserveFailed("order-1", "Out of stock");
    }

    @Test
    void handleInventoryReserveFailed_shouldUseUnknownWhenReasonNull() {
        String message = "{\"orderId\":\"order-1\"}";
        Order order = buildOrder("order-1");

        when(orderSagaService.handleInventoryReserveFailed(eq("order-1"), eq("Unknown")))
                .thenReturn(Mono.just(order));

        handler.handleInventoryReserveFailed(message, "order-1", ack);

        verify(orderSagaService).handleInventoryReserveFailed("order-1", "Unknown");
    }

    @Test
    void handlePaymentCompleted_shouldDelegateToSagaService() {
        String message = "{\"orderId\":\"order-1\"}";
        Order order = buildOrder("order-1");

        when(orderSagaService.handlePaymentCompleted("order-1")).thenReturn(Mono.just(order));

        handler.handlePaymentCompleted(message, "order-1", ack);

        verify(orderSagaService).handlePaymentCompleted("order-1");
    }

    @Test
    void handlePaymentFailed_shouldDelegateWithReason() {
        String message = "{\"orderId\":\"order-1\",\"reason\":\"Insufficient funds\"}";
        Order order = buildOrder("order-1");

        when(orderSagaService.handlePaymentFailed("order-1", "Insufficient funds"))
                .thenReturn(Mono.just(order));

        handler.handlePaymentFailed(message, "order-1", ack);

        verify(orderSagaService).handlePaymentFailed("order-1", "Insufficient funds");
    }

    @Test
    void handlePaymentFailed_shouldAcknowledgeOnBadJson() {
        handler.handlePaymentFailed("not json", "key", ack);
        verify(ack).acknowledge();
    }

    private Order buildOrder(String id) {
        Order order = new Order();
        order.setId(id);
        order.setUserId("user-1");
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of());
        return order;
    }
}
