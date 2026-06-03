package com.ecommerce.order_service.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotBlank String productId,
            @NotBlank String sku,
            @NotBlank String productName,
            @NotNull @Min(1) Integer quantity,
            @NotNull @Positive BigDecimal unitPrice
    ) {}
}
