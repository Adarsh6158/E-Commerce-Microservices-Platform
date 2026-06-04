package com.ecommerce.order_service.Mapper;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Dto.Event.OrderEventItemPayload;
import com.ecommerce.order_service.Dto.Event.OrderEventPayload;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Dto.Response.OrderItemResponse;
import com.ecommerce.order_service.Dto.Response.OrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() != null
                ? order.getItems().stream().map(this::toItemResponse).toList()
                : List.of();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getShippingAddress(),
                order.getCorrelationId(),
                order.getFailureReason(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @Override
    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    @Override
    public OrderEventPayload toEventPayload(Order order) {
        List<OrderEventItemPayload> eventItems = order.getItems() != null
                ? order.getItems().stream()
                .map(i -> new OrderEventItemPayload(
                        i.getProductId(),
                        i.getSku(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList()
                : List.of();

        return new OrderEventPayload(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCorrelationId(),
                order.getFailureReason(),
                eventItems
        );
    }

    @Override
    public List<OrderItem> toOrderItems(List<CreateOrderRequest.Item> items) {
        return items.stream().map(i -> {
            OrderItem item = new OrderItem();
            item.setProductId(i.productId());
            item.setSku(i.sku());
            item.setProductName(i.productName());
            item.setQuantity(i.quantity());
            item.setUnitPrice(i.unitPrice());
            item.setSubtotal(i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())));
            return item;
        }).toList();
    }
}
