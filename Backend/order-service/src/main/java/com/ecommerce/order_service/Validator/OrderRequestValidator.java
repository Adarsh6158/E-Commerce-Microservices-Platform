package com.ecommerce.order_service.Validator;

import com.ecommerce.order_service.Exception.InvalidOrderRequestException;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {

    public void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidOrderRequestException("X-User-Id header must not be blank");
        }
    }

    public void validateOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidOrderRequestException("Order ID must not be blank");
        }
    }

    public void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new InvalidOrderRequestException("Product ID must not be blank");
        }
    }
}
