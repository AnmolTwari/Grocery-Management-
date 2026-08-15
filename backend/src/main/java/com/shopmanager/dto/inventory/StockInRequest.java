package com.shopmanager.dto.inventory;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record StockInRequest(
        @NotNull(message = "Product is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0", inclusive = false, message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @Size(max = 200, message = "Reason must be at most 200 characters")
        String reason) {

    public StockInRequest {
        if (reason != null && reason.isBlank()) {
            reason = null;
        }
    }
}