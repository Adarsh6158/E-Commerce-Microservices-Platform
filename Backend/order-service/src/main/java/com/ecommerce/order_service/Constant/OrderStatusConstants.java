package com.ecommerce.order_service.Constant;

public final class OrderStatusConstants {

    private OrderStatusConstants() {}

    public static final String PENDING = "PENDING";
    public static final String INVENTORY_RESERVED = "INVENTORY_RESERVED";
    public static final String PAYMENT_PROCESSING = "PAYMENT_PROCESSING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";
    public static final String FAILED = "FAILED";
}
