package com.ecommerce.order_service.Service;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Repository.OrderRepository;
import com.ecommerce.order_service.Service.Impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.ecommerce.order_service.Constant.OrderStatusConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OrderMapper orderMapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderEventPublisher, orderMapper);
    }

    @Test
    void createOrder_shouldSaveAndPublishEvent() {
        CreateOrderRequest.Item item = new CreateOrderRequest.Item(
                "prod-1", "SKU-001", "Test Product", 2, new BigDecimal("25.00"));

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId("prod-1");
        orderItem.setSku("SKU-001");
        orderItem.setProductName("Test Product");
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("25.00"));
        orderItem.setSubtotal(new BigDecimal("50.00"));

        when(orderMapper.toOrderItems(any())).thenReturn(List.of(orderItem));

        Order savedOrder = buildOrder("order-1", "user-1", PENDING);
        savedOrder.setItems(List.of(orderItem));
        savedOrder.setTotalAmount(new BigDecimal("50.00"));

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(orderService.createOrder("user-1", List.of(item)))
                .assertNext(order -> {
                    assertThat(order.getId()).isEqualTo("order-1");
                    assertThat(order.getStatus()).isEqualTo(PENDING);
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
                })
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderEvent(eq("order.created"), any(Order.class));
    }

    @Test
    void getOrder_shouldReturnOrder() {
        Order order = buildOrder("order-1", "user-1", CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.getOrder("order-1"))
                .assertNext(o -> assertThat(o.getId()).isEqualTo("order-1"))
                .verifyComplete();
    }

    @Test
    void getOrder_shouldReturnEmptyWhenNotFound() {
        when(orderRepository.findById("nonexistent")).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrder("nonexistent"))
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_shouldReturnUserOrders() {
        Order o1 = buildOrder("order-1", "user-1", CONFIRMED);
        Order o2 = buildOrder("order-2", "user-1", PENDING);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(Flux.just(o1, o2));

        StepVerifier.create(orderService.getOrdersByUserId("user-1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_shouldReturnEmptyForUnknownUser() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("unknown"))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrdersByUserId("unknown"))
                .verifyComplete();
    }

    @Test
    void cancelOrder_shouldCancelPendingOrder() {
        Order order = buildOrder("order-1", "user-1", PENDING);
        Order cancelledOrder = buildOrder("order-1", "user-1", CANCELLED);
        cancelledOrder.setFailureReason("Cancelled by user");

        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(cancelledOrder));

        StepVerifier.create(orderService.cancelOrder("order-1", "user-1"))
                .assertNext(o -> assertThat(o.getStatus()).isEqualTo(CANCELLED))
                .verifyComplete();

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CANCELLED);
    }

    @Test
    void cancelOrder_shouldNotCancelWhenUserMismatch() {
        Order order = buildOrder("order-1", "user-1", PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.cancelOrder("order-1", "user-2"))
                .verifyComplete();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shouldNotCancelConfirmedOrder() {
        Order order = buildOrder("order-1", "user-1", CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.cancelOrder("order-1", "user-1"))
                .verifyComplete();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void hasUserPurchasedProduct_shouldReturnTrueWhenPurchased() {
        Order order = buildOrder("order-1", "user-1", CONFIRMED);
        OrderItem item = new OrderItem();
        item.setProductId("prod-1");
        order.setItems(List.of(item));

        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(Flux.just(order));

        StepVerifier.create(orderService.hasUserPurchasedProduct("user-1", "prod-1"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void hasUserPurchasedProduct_shouldReturnFalseWhenNotPurchased() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.hasUserPurchasedProduct("user-1", "prod-1"))
                .assertNext(result -> assertThat(result).isFalse())
                .verifyComplete();
    }

    @Test
    void hasUserPurchasedProduct_shouldIgnoreNonConfirmedOrders() {
        Order order = buildOrder("order-1", "user-1", PENDING);
        OrderItem item = new OrderItem();
        item.setProductId("prod-1");
        order.setItems(List.of(item));

        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(Flux.just(order));

        StepVerifier.create(orderService.hasUserPurchasedProduct("user-1", "prod-1"))
                .assertNext(result -> assertThat(result).isFalse())
                .verifyComplete();
    }

    private Order buildOrder(String id, String userId, String status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of());
        return order;
    }
}
