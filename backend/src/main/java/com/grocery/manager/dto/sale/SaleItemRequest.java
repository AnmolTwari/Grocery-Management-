package com.grocery.manager.dto.sale;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** A single line of a sale request: product + quantity. */
public record SaleItemRequest(
        @NotNull(message = "Product is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity) {
}