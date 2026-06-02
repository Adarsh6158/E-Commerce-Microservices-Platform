package com.ecommerce.order_service.Exception;

public class OrderCancellationException extends OrderServiceException {

    public OrderCancellationException(String message) {
        super(message);
    }
}
