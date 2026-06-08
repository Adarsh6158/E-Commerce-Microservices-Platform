package com.ecommerce.order_service.Mapper;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Dto.Event.OrderEventPayload;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Dto.Response.OrderItemResponse;
import com.ecommerce.order_service.Dto.Response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperImplTest {

    private OrderMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapperImpl();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Order order = buildOrder();
        OrderResponse response = mapper.toResponse(order);

        assertThat(response.id()).isEqualTo("order-1");
        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.correlationId()).isEqualTo("corr-1");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void toResponse_shouldHandleNullItems() {
        Order order = buildOrder();
        order.setItems(null);

        OrderResponse response = mapper.toResponse(order);
        assertThat(response.items()).isEmpty();
    }

    @Test
    void toItemResponse_shouldMapAllFields() {
        OrderItem item = buildOrderItem();
        OrderItemResponse response = mapper.toItemResponse(item);

        assertThat(response.productId()).isEqualTo("prod-1");
        assertThat(response.sku()).isEqualTo("SKU-001");
        assertThat(response.productName()).isEqualTo("Test Product");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.unitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void toEventPayload_shouldMapAllFields() {
        Order order = buildOrder();
        OrderEventPayload payload = mapper.toEventPayload(order);

        assertThat(payload.orderId()).isEqualTo("order-1");
        assertThat(payload.userId()).isEqualTo("user-1");
        assertThat(payload.status()).isEqualTo("CONFIRMED");
        assertThat(payload.totalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(payload.items()).hasSize(1);
        assertThat(payload.items().get(0).productId()).isEqualTo("prod-1");
    }

    @Test
    void toEventPayload_shouldHandleNullItems() {
        Order order = buildOrder();
        order.setItems(null);

        OrderEventPayload payload = mapper.toEventPayload(order);
        assertThat(payload.items()).isEmpty();
    }

    @Test
    void toOrderItems_shouldConvertRequestItems() {
        CreateOrderRequest.Item reqItem = new CreateOrderRequest.Item(
                "prod-1", "SKU-001", "Test Product", 3, new BigDecimal("10.00"));

        List<OrderItem> items = mapper.toOrderItems(List.of(reqItem));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProductId()).isEqualTo("prod-1");
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
        assertThat(items.get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void toOrderItems_shouldHandleEmptyList() {
        List<OrderItem> items = mapper.toOrderItems(List.of());
        assertThat(items).isEmpty();
    }

    private Order buildOrder() {
        Order order = new Order();
        order.setId("order-1");
        order.setUserId("user-1");
        order.setStatus("CONFIRMED");
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setCorrelationId("corr-1");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of(buildOrderItem()));
        return order;
    }

    private OrderItem buildOrderItem() {
        OrderItem item = new OrderItem();
        item.setProductId("prod-1");
        item.setSku("SKU-001");
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("50.00"));
        return item;
    }
}
