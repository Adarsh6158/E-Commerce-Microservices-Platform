package com.ecommerce.order_service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderServiceApplicationTests {

    @Test
    void applicationClassShouldExist() {
        assertDoesNotThrow(() -> Class.forName("com.ecommerce.order_service.OrderServiceApplication"));
    }
}
