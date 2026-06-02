package com.ecommerce.order_service.Exception;

public class InvalidOrderRequestException extends OrderServiceException {

    public InvalidOrderRequestException(String message) {
        super(message);
    }
}
