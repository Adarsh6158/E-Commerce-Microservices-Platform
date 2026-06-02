package com.ecommerce.order_service.Exception;

public class OrderNotFoundException extends OrderServiceException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
