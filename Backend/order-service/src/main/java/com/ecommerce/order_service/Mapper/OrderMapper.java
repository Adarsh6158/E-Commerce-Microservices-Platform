package com.ecommerce.order_service.Mapper;

import com.ecommerce.order_service.Domain.Order;
import com.ecommerce.order_service.Domain.OrderItem;
import com.ecommerce.order_service.Dto.Event.OrderEventPayload;
import com.ecommerce.order_service.Dto.Request.CreateOrderRequest;
import com.ecommerce.order_service.Dto.Response.OrderItemResponse;
import com.ecommerce.order_service.Dto.Response.OrderResponse;

import java.util.List;

public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);

    OrderEventPayload toEventPayload(Order order);

    List<OrderItem> toOrderItems(List<CreateOrderRequest.Item> items);
}
