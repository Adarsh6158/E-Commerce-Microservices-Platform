package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Repository.OrderRepository;
import com.ecommerce.order_service.Service.Impl.OrderSagaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.ecommerce.order_service.Constant.OrderStatusConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    private OrderSagaServiceImpl sagaService;

    @BeforeEach
    void setUp() {
        sagaService = new OrderSagaServiceImpl(orderRepository, orderEventPublisher);
    }

    @Test
    void updateOrderStatus_shouldUpdateAndSave() {
        Order order = buildOrder("order-1", PENDING);
        Order updated = buildOrder("order-1", CONFIRMED);

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(sagaService.updateOrderStatus("order-1", CONFIRMED, null))
                .assertNext(o -> assertThat(o.getStatus()).isEqualTo(CONFIRMED))
                .verifyComplete();
    }

    @Test
    void updateOrderStatus_shouldSetFailureReason() {
        Order order = buildOrder("order-1", PENDING);
        Order updated = buildOrder("order-1", FAILED);
        updated.setFailureReason("Some reason");

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(sagaService.updateOrderStatus("order-1", FAILED, "Some reason"))
                .assertNext(o -> assertThat(o.getFailureReason()).isEqualTo("Some reason"))
                .verifyComplete();
    }

    @Test
    void updateOrderStatus_shouldReturnEmptyWhenNotFound() {
        when(orderRepository.findById("nonexistent")).thenReturn(Mono.empty());

        StepVerifier.create(sagaService.updateOrderStatus("nonexistent", CONFIRMED, null))
                .verifyComplete();
    }

    @Test
    void handleInventoryReserved_shouldUpdateStatus() {
        Order pending = buildOrder("order-1", PENDING);
        Order reserved = buildOrder("order-1", INVENTORY_RESERVED);
        Order processing = buildOrder("order-1", PAYMENT_PROCESSING);

        when(orderRepository.findById("order-1"))
                .thenReturn(Mono.just(pending))
                .thenReturn(Mono.just(reserved));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(reserved))
                .thenReturn(Mono.just(processing));

        StepVerifier.create(sagaService.handleInventoryReserved("order-1"))
                .assertNext(o -> assertThat(o.getStatus()).isIn(INVENTORY_RESERVED, PAYMENT_PROCESSING))
                .verifyComplete();

        verify(orderRepository, atLeastOnce()).findById("order-1");
    }

    @Test
    void handleInventoryReserveFailed_shouldSetFailedStatus() {
        Order failed = buildOrder("order-1", FAILED);
        failed.setFailureReason("Inventory reservation failed: Out of stock");

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(buildOrder("order-1", PENDING)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(failed));

        StepVerifier.create(sagaService.handleInventoryReserveFailed("order-1", "Out of stock"))
                .assertNext(o -> {
                    assertThat(o.getStatus()).isEqualTo(FAILED);
                    assertThat(o.getFailureReason()).contains("Out of stock");
                })
                .verifyComplete();

        verify(orderEventPublisher).publishOrderEvent(eq("order.failed"), any(Order.class));
    }

    @Test
    void handlePaymentCompleted_shouldConfirmOrder() {
        Order confirmed = buildOrder("order-1", CONFIRMED);

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(buildOrder("order-1", PAYMENT_PROCESSING)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(confirmed));

        StepVerifier.create(sagaService.handlePaymentCompleted("order-1"))
                .assertNext(o -> assertThat(o.getStatus()).isEqualTo(CONFIRMED))
                .verifyComplete();

        verify(orderEventPublisher).publishOrderEvent(eq("order.confirmed"), any(Order.class));
    }

    @Test
    void handlePaymentFailed_shouldFailOrderAndTriggerCompensation() {
        Order failed = buildOrder("order-1", FAILED);
        failed.setFailureReason("Payment failed: Insufficient funds");

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(buildOrder("order-1", PAYMENT_PROCESSING)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(failed));

        StepVerifier.create(sagaService.handlePaymentFailed("order-1", "Insufficient funds"))
                .assertNext(o -> {
                    assertThat(o.getStatus()).isEqualTo(FAILED);
                    assertThat(o.getFailureReason()).contains("Insufficient funds");
                })
                .verifyComplete();

        verify(orderEventPublisher).publishOrderEvent(eq("order.cancelled"), any(Order.class));
    }

    private Order buildOrder(String id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId("user-1");
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of());
        return order;
    }
}
