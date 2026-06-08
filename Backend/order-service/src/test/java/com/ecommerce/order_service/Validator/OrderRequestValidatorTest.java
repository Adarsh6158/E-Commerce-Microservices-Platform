package com.ecommerce.order_service.Validator;

import com.ecommerce.order_service.Exception.InvalidOrderRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderRequestValidatorTest {

    private OrderRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrderRequestValidator();
    }

    @Test
    void validateUserId_shouldPassForValidId() {
        assertDoesNotThrow(() -> validator.validateUserId("user-123"));
    }

    @Test
    void validateUserId_shouldThrowForNull() {
        assertThatThrownBy(() -> validator.validateUserId(null))
                .isInstanceOf(InvalidOrderRequestException.class)
                .hasMessageContaining("X-User-Id");
    }

    @Test
    void validateUserId_shouldThrowForBlank() {
        assertThatThrownBy(() -> validator.validateUserId("   "))
                .isInstanceOf(InvalidOrderRequestException.class);
    }

    @Test
    void validateUserId_shouldThrowForEmpty() {
        assertThatThrownBy(() -> validator.validateUserId(""))
                .isInstanceOf(InvalidOrderRequestException.class);
    }

    @Test
    void validateOrderId_shouldPassForValidId() {
        assertDoesNotThrow(() -> validator.validateOrderId("order-abc"));
    }

    @Test
    void validateOrderId_shouldThrowForNull() {
        assertThatThrownBy(() -> validator.validateOrderId(null))
                .isInstanceOf(InvalidOrderRequestException.class)
                .hasMessageContaining("Order ID");
    }

    @Test
    void validateOrderId_shouldThrowForBlank() {
        assertThatThrownBy(() -> validator.validateOrderId("  "))
                .isInstanceOf(InvalidOrderRequestException.class);
    }

    @Test
    void validateProductId_shouldPassForValidId() {
        assertDoesNotThrow(() -> validator.validateProductId("prod-1"));
    }

    @Test
    void validateProductId_shouldThrowForNull() {
        assertThatThrownBy(() -> validator.validateProductId(null))
                .isInstanceOf(InvalidOrderRequestException.class)
                .hasMessageContaining("Product ID");
    }

    @Test
    void validateProductId_shouldThrowForBlank() {
        assertThatThrownBy(() -> validator.validateProductId(""))
                .isInstanceOf(InvalidOrderRequestException.class);
    }
}
