package com.shopmanager.dto.inventory;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record AdjustmentRequest(
        @NotNull(message = "Product is required")
        Long productId,


        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0", message = "Quantity cannot be negative")
        BigDecimal newQuantity,

        @Size(max = 200, message = "Reason must be at most 200 characters")
        String reason) {

    public AdjustmentRequest {
        if (reason != null && reason.isBlank()) {
            reason = null;
        }
    }
}