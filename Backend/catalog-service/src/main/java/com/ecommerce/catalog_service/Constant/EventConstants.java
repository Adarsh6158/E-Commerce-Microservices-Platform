package com.ecommerce.catalog_service.Constant;

public final class EventConstants {

    private EventConstants() {}


    public static final String TOPIC_PRODUCT_CREATED = "catalog.product.created";
    public static final String TOPIC_PRODUCT_UPDATED = "catalog.product.updated";
    public static final String TOPIC_PRODUCT_DELETED = "catalog.product.deleted";


    public static final String HEADER_CORRELATION_ID = "correlationId";
    public static final String HEADER_EVENT_TYPE = "eventType";


    public static final String EVENT_TYPE_CREATED = "CREATED";
    public static final String EVENT_TYPE_UPDATED = "UPDATED";
    public static final String EVENT_TYPE_DELETED = "DELETED";
}
