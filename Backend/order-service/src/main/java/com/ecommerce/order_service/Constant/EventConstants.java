package com.ecommerce.order_service.Constant;

public final class EventConstants {

    private EventConstants() {}

    public static final String TOPIC_ORDER_CREATED = "order.created";
    public static final String TOPIC_ORDER_PAYMENT_REQUESTED = "order.payment-requested";
    public static final String TOPIC_ORDER_CONFIRMED = "order.confirmed";
    public static final String TOPIC_ORDER_FAILED = "order.failed";
    public static final String TOPIC_ORDER_CANCELLED = "order.cancelled";

    public static final String TOPIC_INVENTORY_RESERVED = "inventory.reserved";
    public static final String TOPIC_INVENTORY_RESERVE_FAILED = "inventory.reserve-failed";
    public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
    public static final String TOPIC_PAYMENT_FAILED = "payment.failed";

    public static final String GROUP_ORDER_SAGA = "order-service-saga";
}
